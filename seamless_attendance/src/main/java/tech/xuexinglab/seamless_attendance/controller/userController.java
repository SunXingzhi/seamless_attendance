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
import tech.xuexinglab.seamless_attendance.DTO.UserResponseDTO;
import tech.xuexinglab.seamless_attendance.entity.user;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/seamless_attendance/api/personnel")
public class userController {
	@Autowired
	private userService userService;

	@GetMapping("users/all")
	public ResponseDTO<List<UserResponseDTO>> getAllUserInfo() {
		List<user> users = userService.getAllUserInfo();
		List<UserResponseDTO> userResponseDTOs = users.stream()
				.map(this::convertToResponseDTO)
				.collect(Collectors.toList());
		return ResponseDTO.success(userResponseDTOs);
	}

	@GetMapping("getUserInfo/{userId}")
	public ResponseDTO<UserResponseDTO> getUserInfo(@PathVariable long userId) {
		user user = userService.getUserInfoById((int) userId);
		UserResponseDTO userResponseDTO = convertToResponseDTO(user);
		return ResponseDTO.success(userResponseDTO);
	}

        // 添加人员信息
        @PostMapping("user/info")
        public ResponseDTO<String> addUserInfo(@RequestBody userDTO userDTO) {
                userService.updateUserInfo(userDTO);
                return ResponseDTO.success("该用户信息添加成功");
        }

        // 更新人员信息
        @PutMapping("user/info/{id}")
        public ResponseDTO<UserResponseDTO> updateUserInfo(@PathVariable long id, @RequestBody userDTO userDTO) {
                userDTO.setUserId((int) id);
                userService.updateUserInfo(userDTO);
                // 获取用户新的信息
                user user = userService.getUserInfoByUserNumber(userDTO.getUser_number());
                UserResponseDTO userResponseDTO = convertToResponseDTO(user);
                return ResponseDTO.success(userResponseDTO);
        }

        // 查看人员考勤记录
        // TODO 分页/最多显示限制
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
        
        // 将user实体转换为UserResponseDTO
        private UserResponseDTO convertToResponseDTO(user user) {
                if (user == null) {
                        return null;
                }
                return new UserResponseDTO(
                        user.getId(),
                        user.getUserNumber(),
                        user.getUserName(),
                        user.getName(),
                        user.getContactType(),
                        user.getContactValue(),
                        user.getRole(),
                        user.getJobTitle(),
                        user.getWorkContent(),
                        user.getStudioId(),
                        user.getStudioAdminId(),
                        user.getAvatar(),
                        user.getStatus(),
                        user.getPairingStatus(),
                        user.getDeviceId(),
                        user.getJoinDate(),
                        user.getCreateTime(),
                        user.getUpdateTime()
                );
        }
}
