package tech.xuexinglab.seamless_attendance.service.interfaces;

import org.springframework.stereotype.Service;
import tech.xuexinglab.seamless_attendance.DTO.ResponseDTO;
import tech.xuexinglab.seamless_attendance.DTO.userDTO;
import tech.xuexinglab.seamless_attendance.entity.user;
import tech.xuexinglab.seamless_attendance.mapper.userMapper;
import tech.xuexinglab.seamless_attendance.entity.attendanceRecord;

import java.util.List;

public interface userService {

        // 获取所有用户信息
        List<user> getAllUserInfo();

        // 根据用户ID获取用户信息
        user getUserInfoById(int id);

        // 根据用户编号获取用户信息
        user getUserInfoByUserNumber(String user_number);

        // 根据用户姓名获取用户信息
        user getUserInfoByUserName(String user_name);

        // 根据用户手机号获取用户信息
        user getUserInfoByPhoneNumber(String phone_number);

        // 更新用户信息
        void updateUserInfo(userDTO user_info);

        // 删除用户信息
        void deleteUserInfo(int id);

        // 获取用户考勤记录
        List<attendanceRecord> getUserAttendanceRecord();
        
        // 用户登录
        user login(String user_name, String password);
        
        // 更新用户密码
        void updatePassword(String user_name, String password);
}
