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
import tech.xuexinglab.seamless_attendance.controller.WebSocketController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
				String userNumber = status.getUser_number();
				double workHours = status.getToday_work_hours();
				
				if (workHours > 0) {
					// 更新考勤记录，保存当天工作时间
					updateAttendanceRecord(userNumber, currentDate, 
							status.getCheck_in_time(), status.getCheck_out_time(), 
							workHours, "active");
					
					// 清零工作时间
					status.setToday_work_hours(0.0);
					status.setCheck_in_time(null);
					status.setCheck_out_time(null);
					status.setUpdate_time(LocalDateTime.now().format(DATETIME_FORMATTER));
					attendanceMapper.updateUserStatus(status);
					
					logger.info("Daily work hours summary for user {}: {} hours", userNumber, workHours);
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

        // 处理人员状态
        private void handlePersonnelStatus(List<String> personnelNumbers, MqttStatusMessage message,
                        String currentDate, String currentDateTime,
                        String currentTimeStr, LocalDateTime currentTime) {
                // 处理P1对应第一个人员
                if (personnelNumbers.size() > 0) {
                        String userNumber = personnelNumbers.get(0);
                        int status = message.getP1();
                        updateUserStatus(userNumber, status, currentDate, currentDateTime, currentTimeStr, currentTime);
                }

                // 处理P2对应第二个人员
                if (personnelNumbers.size() > 1) {
                        String userNumber = personnelNumbers.get(1);
                        int status = message.getP2();
                        updateUserStatus(userNumber, status, currentDate, currentDateTime, currentTimeStr, currentTime);
                }

                // 处理P3对应第三个人员
                if (personnelNumbers.size() > 2) {
                        String userNumber = personnelNumbers.get(2);
                        int status = message.getP3();
                        updateUserStatus(userNumber, status, currentDate, currentDateTime, currentTimeStr, currentTime);
                }
        }

        // 更新用户状态
        private void updateUserStatus(String userNumber, int status,
                        String currentDate, String currentDateTime,
                        String currentTimeStr, LocalDateTime currentTime) {
                try {
                        // 检查用户是否存在
                        user userInfo = userMapper.getUserInfoByUserNumber(userNumber);
                        if (userInfo == null) {
                                logger.error("User not found: {}", userNumber);
                                return;
                        }

                        // 检查是否是假期
                        boolean isHoliday = isHoliday(currentDate);
                        if (isHoliday) {
                                logger.info("Today is a holiday, skipping status update for user: {}", userNumber);
                                return;
                        }

                        // 检查是否是请假
                        boolean isOnLeave = isOnLeave(userNumber, currentDate);
                        if (isOnLeave) {
                                logger.info("User is on leave, skipping status update for user: {}", userNumber);
                                return;
                        }

                        // 获取用户当前状态
                        userStatus currentStatus = attendanceMapper.getUserStatusByUserNumber(userNumber);

                        // 计算工作时间
                        double workHoursToAdd = 0.0;

                        if (currentStatus == null) {
                                // 首次处理该用户
                                currentStatus = new userStatus();
                                currentStatus.setUser_number(userNumber);

                                if (status == 1) {
                                        // 当前在线
                                        currentStatus.setCurrent_status("active");
                                        currentStatus.setLast_active_time(currentDateTime);
                                        currentStatus.setLast_absent_time(null);
                                        currentStatus.setToday_work_hours(0.0);
                                        currentStatus.setCheck_in_time(currentTimeStr);
                                        currentStatus.setCheck_out_time(null);
                                        currentStatus.setCreate_time(currentDateTime);
                                        currentStatus.setUpdate_time(currentDateTime);

                                        // 添加用户状态
                                        attendanceMapper.addUserStatus(currentStatus);

                                        // 更新考勤记录
                                        updateAttendanceRecord(userNumber, currentDate, currentTimeStr, null, 0.0,
                                                        "active");
                                } else {
                                        // 当前离线
                                        currentStatus.setCurrent_status("absent");
                                        currentStatus.setLast_active_time(null);
                                        currentStatus.setLast_absent_time(currentDateTime);
                                        currentStatus.setToday_work_hours(0.0);
                                        currentStatus.setCheck_in_time(null);
                                        currentStatus.setCheck_out_time(null);
                                        currentStatus.setCreate_time(currentDateTime);
                                        currentStatus.setUpdate_time(currentDateTime);

                                        // 添加用户状态
                                        attendanceMapper.addUserStatus(currentStatus);
                                }
                        } else {
                                // 已存在用户状态
                                String previousStatus = currentStatus.getCurrent_status();

                                if (status == 1) {
                                        // 当前在线
                                        if ("absent".equals(previousStatus)) {
                                                // 从离线变为在线
                                                currentStatus.setCurrent_status("active");
                                                currentStatus.setLast_active_time(currentDateTime);
                                                currentStatus.setUpdate_time(currentDateTime);

                                                // 如果今天还没有打卡，记录打卡时间
                                                if (currentStatus.getCheck_in_time() == null) {
                                                        // TODO: 当后续增加任务处理时,可以查询该人员是否迟到
                                                        currentStatus.setCheck_in_time(currentTimeStr);
                                                }

                                                // 更新用户状态
                                                attendanceMapper.updateUserStatus(currentStatus);
                                        }
                                        // 保持在线状态，不做操作
                                } else {
                                        // 当前离线
                                        if ("active".equals(previousStatus)) {
                                                // 从在线变为离线
                                                // 计算工作时间
                                                if (currentStatus.getLast_active_time() != null) {
                                                        LocalDateTime lastactiveTime = LocalDateTime.parse(
                                                                        currentStatus.getLast_active_time(),
                                                                        DATETIME_FORMATTER);
                                                        Duration duration = Duration.between(lastactiveTime,
                                                                        currentTime);
                                                        workHoursToAdd = duration.toMinutes() / 60.0;
                                                }

                                                // 更新用户状态
                                                currentStatus.setCurrent_status("leave");
                                                currentStatus.setLast_absent_time(currentDateTime);
                                                currentStatus.setToday_work_hours(
                                                                currentStatus.getToday_work_hours() + workHoursToAdd);
                                                currentStatus.setCheck_out_time(currentTimeStr);
                                                currentStatus.setUpdate_time(currentDateTime);

                                                // 更新用户状态
                                                attendanceMapper.updateUserStatus(currentStatus);
                                                // 更新user表中的status字段
                                                userMapper.updateUserStatus(userNumber, "leave");
                                                // 更新考勤记录
                                                updateAttendanceRecord(userNumber, currentDate,
                                                                currentStatus.getCheck_in_time(),
                                                                currentTimeStr,
                                                                currentStatus.getToday_work_hours(),
                                                                "active");
                                        }
                                        // 保持离线状态，不做操作
                                }
                        }

                        // 更新user表中的status字段
                        if (userInfo != null) {
                                userInfo.setStatus(status == 1 ? "active" : "absent");
                                // 使用现有的updateUserInfo方法更新用户状态
                                userMapper.updateUserInfo(
                                                userInfo.getId(),
                                                userInfo.getName(),
                                                userInfo.getContact_type(),
                                                userInfo.getContact_value(),
                                                userInfo.getUser_number(),
                                                userInfo.getRole(),
                                                userInfo.getJob_title(),
                                                userInfo.getWork_content(),
                                                userInfo.getStudio_id(),
                                                userInfo.getAvatar(),
                                                userInfo.getStatus(),
                                                userInfo.getJoin_date(),
                                                userInfo.getDevice_id());
                        }

                        // 发送WebSocket消息到前端
                        sendStatusUpdateToFrontend(userNumber, status == 1 ? "active" : "absent", currentDateTime);

                } catch (Exception e) {
                        logger.error("Error updating user status: {}", userNumber, e);
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

                        if (record == null) {
                                // 添加新记录
                                record = new attendanceRecord();
                                record.setUser_number(userNumber);
                                record.setDate(date);
                                record.setCheck_in_time(checkInTime);
                                record.setCheck_out_time(checkOutTime);
                                record.setWork_hours(workHours);
                                record.setStatus(status);
                                record.setCreate_time(currentTime);
                                record.setUpdate_time(currentTime);
                                attendanceMapper.addAttendanceRecord(record);
                        } else {
                                // 更新现有记录
                                record.setCheck_in_time(checkInTime);
                                record.setCheck_out_time(checkOutTime);
                                record.setWork_hours(workHours);
                                record.setStatus(status);
                                record.setUpdate_time(currentTime);
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
                        // 简单的JSON解析，实际项目中可以使用JSON库
                        MqttStatusMessage message = new MqttStatusMessage();

                        // 提取ID
                        int idStart = payload.indexOf("\"ID\":\"") + 5;
                        int idEnd = payload.indexOf("\"", idStart);
                        if (idStart > 4 && idEnd > idStart) {
                                message.setId(payload.substring(idStart, idEnd));
                        }

                        // 提取P1
                        int p1Start = payload.indexOf("\"P1\":") + 5;
                        int p1End = payload.indexOf(",", p1Start);
                        if (p1End == -1)
                                p1End = payload.indexOf("}", p1Start);
                        if (p1Start > 4 && p1End > p1Start) {
                                message.setP1(Integer.parseInt(payload.substring(p1Start, p1End).trim()));
                        }

                        // 提取P2
                        int p2Start = payload.indexOf("\"P2\":") + 5;
                        int p2End = payload.indexOf(",", p2Start);
                        if (p2End == -1)
                                p2End = payload.indexOf("}", p2Start);
                        if (p2Start > 4 && p2End > p2Start) {
                                message.setP2(Integer.parseInt(payload.substring(p2Start, p2End).trim()));
                        }

                        // 提取P3
                        int p3Start = payload.indexOf("\"P3\":") + 5;
                        int p3End = payload.indexOf(",", p3Start);
                        if (p3End == -1)
                                p3End = payload.indexOf("}", p3Start);
                        if (p3Start > 4 && p3End > p3Start) {
                                message.setP3(Integer.parseInt(payload.substring(p3Start, p3End).trim()));
                        }

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
