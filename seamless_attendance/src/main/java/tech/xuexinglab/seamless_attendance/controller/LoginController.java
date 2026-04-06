package tech.xuexinglab.seamless_attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import tech.xuexinglab.seamless_attendance.DTO.ResponseDTO;
import tech.xuexinglab.seamless_attendance.entity.user;
import tech.xuexinglab.seamless_attendance.service.interfaces.userService;

@RestController
@RequestMapping("/seamless_attendance/api/auth")
public class LoginController {

	@Autowired
	private userService userService;

	@PostMapping("/login")
	public ResponseDTO<?> login(@RequestBody LoginRequest loginRequest) {
		String username = loginRequest.getUsername();
		String password = loginRequest.getPassword();

		if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
			return ResponseDTO.error("用户名和密码不能为空");
		}

		if (username.length() > 10) {
			return ResponseDTO.error("用户名不能超过10个字符");
		}

		if (password.contains(" ") || password.contains("\t") || password.contains("\n")) {
			return ResponseDTO.error("密码不能包含空白字符");
		}

		user user = userService.login(username, password);
		if (user == null) {       // 登录失败
			return ResponseDTO.error("用户名或密码错误");
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setId(user.getId());
		userInfo.setName(user.getName());
		userInfo.setUsername(user.getUserName());
		userInfo.setRole(user.getRole());
		userInfo.setStudioId(user.getStudioId());
		userInfo.setStudioAdminId(user.getStudioAdminId());

		return ResponseDTO.success(userInfo);
	}

	static class LoginRequest {
		private String username;
		private String password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	static class UserInfo {
		private Integer id;
		private String name;
		private String username;
		private String role;
		private String studioId;
		private Integer studioAdminId;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public String getStudioId() {
			return studioId;
		}

		public void setStudioId(String studioId) {
			this.studioId = studioId;
		}

		public Integer getStudioAdminId() {
			return studioAdminId;
		}

		public void setStudioAdminId(Integer studioAdminId) {
			this.studioAdminId = studioAdminId;
		}
	}
}
