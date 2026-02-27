package tech.xuexinglab.seamless_attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import tech.xuexinglab.seamless_attendance.service.interfaces.userService;
import tech.xuexinglab.seamless_attendance.DTO.userDTO;
import tech.xuexinglab.seamless_attendance.DTO.ResponseDTO;
import tech.xuexinglab.seamless_attendance.entity.user;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/seamless_attendance/api/personnel")
public class userController {
	@Autowired
	private userService userService;

	@GetMapping("users/all")
	public ResponseDTO<List<user>> getAllUserInfo() {
		return ResponseDTO.success(userService.getAllUserInfo());
	}

	@GetMapping("getUserInfo/{userId}")
	public ResponseDTO<user> getUserInfo(@PathVariable long userId) {
		return ResponseDTO.success(userService.getUserInfoById((int) userId));
	}

        // 添加人员信息
        @PostMapping("user/info")
        public ResponseDTO<String> addUserInfo(@RequestBody userDTO userDTO) {
                userService.updateUserInfo(userDTO);
                return ResponseDTO.success("该用户信息添加成功");
        }

        // 更新人员信息
        @PutMapping("user/info/{id}")
        public ResponseDTO<String> updateUserInfo(@PathVariable long id, @RequestBody userDTO userDTO) {
                userDTO.setUserId((int) id);
                userService.updateUserInfo(userDTO);
                return ResponseDTO.success("该用户信息更新成功");
        }

        // 查看人员考勤记录
        // to do 分页/最多显示限制
        @GetMapping("users/attendanceRecords/all")
        public ResponseDTO<?> getUserAttendanceRecord() {
                // debug
                return ResponseDTO.success(userService.getUserAttendanceRecord());
        }

        @DeleteMapping("user/info/{id}")
        public ResponseDTO<String> deleteUserInfo(@PathVariable long id) {
                userService.deleteUserInfo((int) id);
                return ResponseDTO.success("该用户信息删除成功");
        }
}
