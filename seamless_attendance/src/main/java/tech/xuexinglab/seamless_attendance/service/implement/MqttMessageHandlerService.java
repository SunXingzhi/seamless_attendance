package tech.xuexinglab.seamless_attendance.service.implement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.xuexinglab.seamless_attendance.entity.attendanceRecord;
import tech.xuexinglab.seamless_attendance.entity.userStatus;
import tech.xuexinglab.seamless_attendance.entity.leaveRecord;
import tech.xuexinglab.seamless_attendance.entity.holidayRecord;
import tech.xuexinglab.seamless_attendance.entity.device;
import tech.xuexinglab.seamless_attendance.entity.user;
import tech.xuexinglab.seamless_attendance.mapper.attendanceMapper;
import tech.xuexinglab.seamless_attendance.mapper.deviceMapper;
import tech.xuexinglab.seamless_attendance.mapper.userMapper;
import tech.xuexinglab.seamless_attendance.service.MqttMessageSender;
import tech.xuexinglab.seamless_attendance.configuration.MqttProperties;
import tech.xuexinglab.seamless_attendance.controller.WebSocketController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.alibaba.fastjson.JSONObject;

@Service
public class MqttMessageHandlerService {
        private static final Logger logger = LoggerFactory.getLogger(MqttMessageHandlerService.class);

        @Autowired
        private attendanceMapper attendanceMapper;

        @Autowired
        private deviceMapper deviceMapper;

        @Autowired
        private userMapper userMapper;

        @Autowired
        private WebSocketController webSocketController;

        @Autowired
        private MqttMessageSender messageSender;

        @Autowired
        private MqttProperties mqttProperties;

        // 日期时间格式器
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
        private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 每天固定时间（晚上11点）执行工作时间统计和清零
        @Scheduled(cron = "0 0 23 * * ?")
        public void dailyWorkHoursSummary() {
                try {
                        logger.info("Starting daily work hours summary");

                        // 获取当前日期
                        String currentDate = LocalDateTime.now().format(DATE_FORMATTER);

                        // 获取所有用户的状态
                        List<userStatus> allUserStatuses = attendanceMapper.getAllUserStatuses();

                        for (userStatus status : allUserStatuses) {
                                String userNumber = status.getUserNumber();
                                double workHours = status.getTodayWorkHours();

                                if (workHours > 0) {
                                        // 更新考勤记录，保存当天工作时间
                                        updateAttendanceRecord(userNumber, currentDate,
                                                        status.getCheckInTime(), status.getCheckOutTime(),
                                                        workHours, "active");

                                        // 清零工作时间
                                        status.setTodayWorkHours(0.0);
                                        status.setCheckInTime(null);
                                        status.setCheckOutTime(null);
                                        status.setUpdateTime(LocalDateTime.now().format(DATETIME_FORMATTER));
                                        attendanceMapper.updateUserStatus(status);

                                        logger.info("Daily work hours summary for user {}: {} hours", userNumber,
                                                        workHours);
                                }
                        }

                        logger.info("Daily work hours summary completed");
                } catch (Exception e) {
                        logger.error("Error in daily work hours summary", e);
                }
        }

        // 处理MQTT状态消息
        public void handleStatusMessage(String deviceId, String payload) {
                try {
                        logger.info("Handling status message for device: {}", deviceId);

                        // 解析MQTT消息
                        MqttStatusMessage message = parseMqttStatusMessage(payload);
                        if (message == null) {
                                logger.error("Failed to parse MQTT status message: {}", payload);
                                return;
                        }

                        // 根据设备ID获取设备信息
                        device deviceInfo = deviceMapper.getDeviceByDeviceName(deviceId);
                        if (deviceInfo == null) {
                                logger.error("Device not found: {}", deviceId);
                                return;
                        }

                        // 解析设备的人员列表
                        List<String> personnelNumbers = parsePersonnelNumbers(deviceInfo.getPersonnels());
                        if (personnelNumbers == null || personnelNumbers.isEmpty()) {
                                logger.error("No personnel assigned to device: {}", deviceId);
                                return;
                        }

                        // 获取当前时间
                        LocalDateTime currentTime = LocalDateTime.now();
                        String currentDate = currentTime.format(DATE_FORMATTER);
                        String currentDateTime = currentTime.format(DATETIME_FORMATTER);
                        String currentTimeStr = currentTime.format(TIME_FORMATTER);

                        // 处理每个人员的状态
                        handlePersonnelStatus(personnelNumbers, message, currentDate, currentDateTime, currentTimeStr,
                                        currentTime);

                } catch (Exception e) {
                        logger.error("Error handling MQTT status message", e);
                }
        }

        // 处理人员状态, 根据MQTT消息中的P1、P2、P3字段更新对应人员的状态, 并发送人员信息到硬件平台
        private void handlePersonnelStatus(List<String> personnelNumbers, MqttStatusMessage message,
                        String currentDate, String currentDateTime,
                        String currentTimeStr, LocalDateTime currentTime) {

                String result_status = "absent"; // 默认状态

                // 如果根据设备没有找到任何人员, 则不处理状态更新, 直接返回
                if (personnelNumbers == null || personnelNumbers.isEmpty()) {
                        logger.warn("No personnel numbers provided for device, skipping status update");
                        return;
                }

                // 发送的人员数据不应该比设备绑定的人员数据更多, 但可以更少(如果某些人员状态没有更新)
                if (personnelNumbers.size() > 3) {
                        logger.error("More than 3 personnel assigned to device, only processing the first 3");
                        return;
                }

                if (personnelNumbers.size() < 3) {
                        logger.warn("Less than 3 personnel assigned to device, some status may not be updated");
                }

                try{
                        // 获取设备的原始设备名称(用于发送MQTT消息时的设备标识, 例如device_name:A1, origin_device_name:A)
                        String originDeviceId = message.id.replaceAll("[_\\d]+$", ""); // 移除末尾的数字和下划线
                        // 处理P1对应第一个人员
                        if (personnelNumbers.size() > 0) {
                                String userNumber = personnelNumbers.get(0);
                                int status = message.getP1();
                                // 判断人员状态, 只有状态更新时才进行后续处理, 避免重复更新和发送消息
                                if (!hasStatusChanged(userNumber, String.valueOf(status))) {
                                        logger.info("Status for user {} has not changed, skipping update", userNumber);
                                        return;
                                }
                                // 这里应该确保传入的是学号, 而不是人员姓名
                                result_status = updateUserStatus(userNumber, status, currentDate, currentDateTime,
                                                currentTimeStr, currentTime);
                                
                                // 发送WebSocket消息到前端
                                sendStatusUpdateToFrontend(userNumber, result_status, currentDateTime);
                                // 发送人员信息到硬件平台
                                sendStatusToHardware(originDeviceId, "1", result_status);
                        }

                        // 处理P2对应第二个人员
                        if (personnelNumbers.size() > 1) {
                                String userNumber = personnelNumbers.get(1);
                                int status = message.getP2();
                                // 判断人员状态, 只有状态更新时才进行后续处理, 避免重复更新和发送消息
                                if (!hasStatusChanged(userNumber, String.valueOf(status))) {
                                        logger.info("Status for user {} has not changed, skipping update", userNumber);
                                        return;
                                }
                                result_status = updateUserStatus(userNumber, status, currentDate, currentDateTime,
                                                currentTimeStr, currentTime);
                                // 发送WebSocket消息到前端
                                sendStatusUpdateToFrontend(userNumber, result_status, currentDateTime);
                                // 发送人员信息到硬件平台
                                sendStatusToHardware(originDeviceId, "2", result_status);
                        }

                        // 处理P3对应第三个人员
                        if (personnelNumbers.size() > 2) {
                                String userNumber = personnelNumbers.get(2);
                                int status = message.getP3();
                                // 判断人员状态, 只有状态更新时才进行后续处理, 避免重复更新和发送消息
                                if (!hasStatusChanged(userNumber, String.valueOf(status))) {
                                        logger.info("Status for user {} has not changed, skipping update", userNumber);
                                        return;
                                }
                                result_status = updateUserStatus(userNumber, status, currentDate, currentDateTime,
                                                currentTimeStr, currentTime);
                                // 发送WebSocket消息到前端
                                sendStatusUpdateToFrontend(userNumber, result_status, currentDateTime);
                                // 发送人员信息到硬件平台
                                sendStatusToHardware(originDeviceId, "3", result_status);        
                        }
                } 
                catch (Exception e) {
                        logger.error("Error handling personnel status", e);
                }

        }
        // 判断人员状态是否发生改变
        private boolean hasStatusChanged(String userNumber, String newStatus) {
                try {
                        userStatus currentStatus = attendanceMapper.getUserStatusByUserNumber(userNumber);
                        if (currentStatus == null) {
                                // 如果没有现有状态，认为状态发生了改变
                                return true;
                        }
                        return !newStatus.equals(currentStatus.getCurrentStatus());
                } catch (Exception e) {
                        logger.error("Error checking status change for user: {}", userNumber, e);
                        // 在发生错误时，默认认为状态发生了改变，以确保后续处理能够进行
                        return true;
                }
        }

        // 发送人员状态到硬件平台, 这里不
        private void sendStatusToHardware(String originDeviceId, String index, String result_status) {
                try {
                        // 将 result_status 映射为 DATA 值
                        int data = "active".equals(result_status) ? 1 : 0;
                        switch (result_status.toLowerCase()) {
                                case "late":
                                        data    = 1;
                                        break;
                                case "absent":
                                        data    = 3;
                                        break;
                                case "active":
                                        data    = 2;
                                        break;
                                
                                case "excused":
                                        data    = 4;
                                        break;
                                case "holiday":
                                        data    = 5;
                                        break;
                                case "leave":
                                        data    = 6;
                                        break;
                                default:
                                        data    = 3;
                                        break;
                        }
                        // 构建JSON消息
                        String jsonMessage = String.format(
                                        "{\"ID\":\"%s\",\"INDEX\":%s,\"TYPE\":\"status\",\"DATA\":%d}",
                                        originDeviceId, index, data);

                        // 发送到 show 主题
                        messageSender.sendMessage(mqttProperties.showTopic, jsonMessage);
                        logger.info("Sent status to hardware: {}", jsonMessage);
                } catch (Exception e) {
                        logger.error("Error sending status to hardware", e);
                }
        }
        
        // 更新用户状态
        private String updateUserStatus(String userNumber, int status,
                        String currentDate, String currentDateTime,
                        String currentTimeStr, LocalDateTime currentTime) {

                String result_status;

                result_status = status == 1 ? "active" : "absent";
                try {
                        // 检查用户是否存在
                        user userInfo = userMapper.getUserInfoByUserNumber(userNumber);

                        if (userInfo == null) {
                                userInfo = userMapper.getUserInfoByName(userNumber);
                                if (userInfo == null) {
                                        logger.error("User number not found: {}", userNumber);
                                        return null;
                                } else {
                                        userNumber = userInfo.getUserNumber();
                                        logger.info("Use username for get user information.");
                                }
                        }

                        // 检查是否是假期: 优先级更高
                        boolean isHoliday = isHoliday(currentDate);
                        if (isHoliday) {
                                logger.info("Today is a holiday, skipping status update for user: {}", userNumber);
                                return "holiday";
                        }

                        // 检查是否是请假
                        boolean isOnLeave = isOnLeave(userNumber, currentDate);
                        if (isOnLeave) {
                                logger.info("User is on leave, skipping status update for user: {}", userNumber);
                                return "excused";
                        }

                        // 获取用户当前状态
                        userStatus currentStatus = attendanceMapper.getUserStatusByUserNumber(userNumber);

                        // 计算工作时间
                        double workHoursToAdd = 0.0;

                        if (currentStatus == null) {
                                // 首次处理该用户
                                currentStatus = new userStatus();
                                currentStatus.setUserNumber(userNumber);

                                if (status == 1) {
                                        // 当前在线
                                        currentStatus.setCurrentStatus("active");
                                        currentStatus.setLastActiveTime(currentDateTime);
                                        currentStatus.setLastAbsentTime(null);
                                        currentStatus.setTodayWorkHours(0.0);
                                        currentStatus.setCheckInTime(currentTimeStr);
                                        currentStatus.setCheckOutTime(null);
                                        currentStatus.setCreateTime(currentDateTime);
                                        currentStatus.setUpdateTime(currentDateTime);

                                        // 添加用户状态
                                        attendanceMapper.addUserStatus(currentStatus);
                                        result_status = "active";
                                        // 更新考勤记录
                                        updateAttendanceRecord(userNumber, currentDate, currentTimeStr, null, 0.0,
                                                        "active");
                                } else {
                                        // 当前离线
                                        currentStatus.setCurrentStatus("absent");
                                        currentStatus.setLastActiveTime(null);
                                        currentStatus.setLastAbsentTime(currentDateTime);
                                        currentStatus.setTodayWorkHours(0.0);
                                        currentStatus.setCheckInTime(null);
                                        currentStatus.setCheckOutTime(null);
                                        currentStatus.setCreateTime(currentDateTime);
                                        currentStatus.setUpdateTime(currentDateTime);

                                        // 添加用户状态
                                        attendanceMapper.addUserStatus(currentStatus);
                                        result_status = "absent";
                                }
                        } else {
                                // 已存在用户状态
                                String previousStatus = currentStatus.getCurrentStatus();

                                if (status == 1) {
                                        // 当前在线
                                        if ("absent".equals(previousStatus)) {
                                                // 从离线变为在线
                                                currentStatus.setCurrentStatus("active");
                                                currentStatus.setLastActiveTime(currentDateTime);
                                                currentStatus.setUpdateTime(currentDateTime);

                                                // 如果今天还没有打卡，记录打卡时间
                                                if (currentStatus.getCheckInTime() == null) {
                                                        // TODO: 当后续增加任务处理时,可以查询该人员是否迟到
                                                        currentStatus.setCheckInTime(currentTimeStr);
                                                }

                                                // 更新用户状态
                                                attendanceMapper.updateUserStatus(currentStatus);
                                                result_status = "active";
                                        }
                                        // 保持在线状态，不做操作
                                } else {
                                        // 当前离线
                                        if ("active".equals(previousStatus)) {
                                                // 从在线变为离线
                                                // 计算工作时间
                                                if (currentStatus.getLastActiveTime() != null) {
                                                        LocalDateTime lastactiveTime = LocalDateTime.parse(
                                                                        currentStatus.getLastActiveTime(),
                                                                        DATETIME_FORMATTER);
                                                        Duration duration = Duration.between(lastactiveTime,
                                                                        currentTime);
                                                        workHoursToAdd = duration.toMinutes() / 60.0;
                                                }

                                                // 更新用户状态
                                                currentStatus.setCurrentStatus("leave");
                                                currentStatus.setLastAbsentTime(currentDateTime);
                                                currentStatus.setTodayWorkHours(
                                                                currentStatus.getTodayWorkHours() + workHoursToAdd);
                                                currentStatus.setCheckOutTime(currentTimeStr);
                                                currentStatus.setUpdateTime(currentDateTime);

                                                // 更新用户状态
                                                attendanceMapper.updateUserStatus(currentStatus);
                                                result_status = "leave";
                                                // 更新user表中的status字段
                                                userMapper.updateUserStatus(userNumber, "leave");
                                                // 更新考勤记录
                                                updateAttendanceRecord(userNumber, currentDate,
                                                                currentStatus.getCheckInTime(),
                                                                currentTimeStr,
                                                                currentStatus.getTodayWorkHours(),
                                                                "leave");
                                        }
                                        // 保持离线状态，不做操作
                                }
                        }

                        // 更新user表中的status字段
                        if (userInfo != null) {
                                // userInfo.setStatus(status == 1 ? "active" : "absent");
                                userInfo.setStatus(result_status);
                                // 使用现有的updateUserInfo方法更新用户状态
                                userMapper.updateUserInfo(
                                                userInfo.getId(),
                                                userInfo.getName(),
                                                userInfo.getContactType(),
                                                userInfo.getContactValue(),
                                                userInfo.getUserNumber(),
                                                userInfo.getRole(),
                                                userInfo.getJobTitle(),
                                                userInfo.getWorkContent(),
                                                userInfo.getStudioId(),
                                                userInfo.getAvatar(),
                                                userInfo.getStatus(),
                                                userInfo.getJoinDate(),
                                                userInfo.getDeviceId());
                        }

                        // // 发送WebSocket消息到前端
                        // sendStatusUpdateToFrontend(userNumber, result_status, currentDateTime);
                        return result_status;
                } catch (Exception e) {
                        logger.error("Error updating user status: {}", userNumber, e);
                        return null;
                }
        }

        // 更新考勤记录
        private void updateAttendanceRecord(String userNumber, String date,
                        String checkInTime, String checkOutTime,
                        double workHours, String status) {
                try {
                        attendanceRecord record = attendanceMapper.getAttendanceRecordByUserNumberAndDate(userNumber,
                                        date);
                        String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);

                        // 提取时间部分（如果字符串包含空格，则提取空格后的部分）
                        String timePartIn = null;
                        if (checkInTime != null) {
                                if (checkInTime.contains(" ")) {
                                        String[] parts = checkInTime.split(" ");
                                        timePartIn = parts.length >= 2 ? parts[1] : checkInTime;
                                } else {
                                        timePartIn = checkInTime;
                                }
                        }
                        String timePartOut = null;
                        if (checkOutTime != null) {
                                if (checkOutTime.contains(" ")) {
                                        String[] parts = checkOutTime.split(" ");
                                        timePartOut = parts.length >= 2 ? parts[1] : checkOutTime;
                                } else {
                                        timePartOut = checkOutTime;
                                }
                        }

                        // 构建完整的日期时间字符串
                        String fullCheckInTime = timePartIn != null ? date + " " + timePartIn : null;
                        String fullCheckOutTime = timePartOut != null ? date + " " + timePartOut : null;

                        if (record == null) {
                                // 添加新记录
                                record = new attendanceRecord();
                                record.setUserNumber(userNumber);
                                record.setDate(date);
                                record.setCheckInTime(fullCheckInTime);
                                record.setCheckOutTime(fullCheckOutTime);
                                record.setWorkHours(workHours);
                                record.setStatus(status);
                                record.setCreateTime(currentTime);
                                record.setUpdateTime(currentTime);
                                attendanceMapper.addAttendanceRecord(record);
                        } else {
                                // 更新现有记录
                                record.setCheckInTime(fullCheckInTime);
                                record.setCheckOutTime(fullCheckOutTime);
                                record.setWorkHours(workHours);
                                record.setStatus(status);
                                record.setUpdateTime(currentTime);
                                attendanceMapper.updateAttendanceRecord(record);
                        }
                } catch (Exception e) {
                        logger.error("Error updating attendance record: {}", userNumber, e);
                }
        }

        // 检查是否是假期
        private boolean isHoliday(String date) {
                try {
                        holidayRecord holiday = attendanceMapper.getHolidayRecordByDate(date);
                        return holiday != null;
                } catch (Exception e) {
                        logger.error("Error checking holiday status", e);
                        return false;
                }
        }

        // 检查是否是请假
        private boolean isOnLeave(String userNumber, String date) {
                try {
                        leaveRecord leave = attendanceMapper.getLeaveRecordByUserNumberAndDate(userNumber, date);
                        return leave != null;
                } catch (Exception e) {
                        logger.error("Error checking leave status", e);
                        return false;
                }
        }

        // 发送状态更新到前端
        private void sendStatusUpdateToFrontend(String userNumber, String status, String timestamp) {
                try {
                        // 构建状态更新消息
                        String message = String.format("{\"userNumber\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}",
                                        userNumber, status, timestamp);

                        // 发送WebSocket消息
                        webSocketController.sendMessageToAll(message);
                        logger.info("Sent status update to frontend for user: {}", userNumber);
                } catch (Exception e) {
                        logger.error("Error sending status update to frontend", e);
                }
        }

        // 解析MQTT状态消息
        private MqttStatusMessage parseMqttStatusMessage(String payload) {
                try {
                        JSONObject jsonObject = JSONObject.parseObject(payload);
                        MqttStatusMessage message = new MqttStatusMessage();
                        message.setId(jsonObject.getString("ID"));
                        message.setP1(jsonObject.getInteger("P1"));
                        message.setP2(jsonObject.getInteger("P2"));
                        message.setP3(jsonObject.getInteger("P3"));
                        return message;
                } catch (Exception e) {
                        logger.error("Error parsing MQTT status message", e);
                        return null;
                }
        }

        // 解析设备的人员列表
        private List<String> parsePersonnelNumbers(String personnels) {
                if (personnels == null || personnels.isEmpty()) {
                        return null;
                }

                try {
                        String[] numbers = personnels.split(",");
                        return Arrays.stream(numbers)
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .collect(Collectors.toList());
                } catch (Exception e) {
                        logger.error("Error parsing personnel numbers", e);
                        return null;
                }
        }

        // MQTT状态消息模型
        private static class MqttStatusMessage {
                private String id;
                private int p1;
                private int p2;
                private int p3;

                public void setId(String id) {
                        this.id = id;
                }

                public int getP1() {
                        return p1;
                }

                public void setP1(int p1) {
                        this.p1 = p1;
                }

                public int getP2() {
                        return p2;
                }

                public void setP2(int p2) {
                        this.p2 = p2;
                }

                public int getP3() {
                        return p3;
                }

                public void setP3(int p3) {
                        this.p3 = p3;
                }
        }
}
