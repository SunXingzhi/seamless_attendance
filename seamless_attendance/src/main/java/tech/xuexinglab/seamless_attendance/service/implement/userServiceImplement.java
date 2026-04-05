package tech.xuexinglab.seamless_attendance.service.implement;

import org.springframework.stereotype.Service;
import tech.xuexinglab.seamless_attendance.service.interfaces.userService;

import tech.xuexinglab.seamless_attendance.DTO.userDTO;
import tech.xuexinglab.seamless_attendance.entity.user;
import tech.xuexinglab.seamless_attendance.mapper.userMapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import tech.xuexinglab.seamless_attendance.entity.attendanceRecord;

@Service
public class userServiceImplement implements userService {
	@Autowired
	private userMapper userMapper;

	@Override
	public List<user> getAllUserInfo() {
		return userMapper.getAllUserInfo();
	}

	@Override
	public user getUserInfoById(int id) {
		return userMapper.getUserInfoById(id);
	}

	@Override
	public user getUserInfoByUserNumber(String user_number) {
		return userMapper.getUserInfoByUserNumber(user_number);
	}

	@Override
	public user getUserInfoByPhoneNumber(String phone_number) {
		return userMapper.getUserInfoByPhone(phone_number);
	}

	@Override
	public user getUserInfoByUserName(String user_name) {
		return userMapper.getUserInfoByName(user_name);
	}

	@Override
        public void updateUserInfo(userDTO user_info) {
                // 获取当前日期作为默认值
                String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                // 为 work_content 提供默认值，以防为 null
                String workContent = user_info.getWork_task() != null ? user_info.getWork_task() : "";
                // 为 device_id 提供默认值，以防为 null
                Integer deviceId = 0;
                
                if((user_info.getUserId() == null)){
                        // 视为添加人员
                        userMapper.addUserInfo( 
                                        user_info.getName(), 
                                        user_info.getContact_value(), 
                                        user_info.getUser_number(),
                                        user_info.getRole(), 
                                        "", // job_title
                                        workContent, // work_content
                                        user_info.getStudio_id(), // studio_id
                                        "", // avatar
                                        "", // status
                                        currentDate, // join_date
                                        deviceId // device_id
						);
			return;
		}
		else{
			userMapper.updateUserInfo( 
				user_info.getUserId(),
				user_info.getName(), 
				user_info.getContact_type(), 
				user_info.getContact_value(), 
				user_info.getUser_number(),
				user_info.getRole(), 
				user_info.getWork_task() != null ? user_info.getWork_task() : "", // job_title
				workContent, // work_content
				user_info.getStudio_id(), // studio_id
				"", // avatar
				"", // status
				currentDate, // join_date
				deviceId // device_id
						);
		}
						
        }

	@Override
	public void deleteUserInfo(int id) {
		// 删除用户
		userMapper.deleteUserInfo(id);
		// 重新编号用户ID，保持连续性
		userMapper.renumberUserIds();
		// 重置用户表自增计数器
		userMapper.resetUserAutoIncrement();
	}

	@Override
	public List<attendanceRecord> getUserAttendanceRecord() {
		List<attendanceRecord> attendanceRecords = userMapper.getUserAttendanceRecord();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		for (attendanceRecord record : attendanceRecords) {
			if (record.getCheck_in_time() != null && record.getCheck_out_time() != null) {
				try {
					LocalDateTime checkIn = LocalDateTime.parse(record.getCheck_in_time(),
							formatter);
					LocalDateTime checkOut = LocalDateTime.parse(record.getCheck_out_time(),
							formatter);
					long hours = java.time.Duration.between(checkIn, checkOut).toHours();
					record.setWork_hours((double) hours);

				} catch (Exception e) {
					record.setWork_hours(0.0);
				}
			} else {
				record.setWork_hours(0.0);
			}
		}
		return attendanceRecords;
	}
	
	@Override
	public user login(String user_name, String password) {
		return userMapper.login(user_name, password);
	}
	
	@Override
        public void updatePassword(String user_name, String password) {
		userMapper.updatePassword(user_name, password);
	}
}
