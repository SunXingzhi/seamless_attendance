package tech.xuexinglab.seamless_attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class administrator {
	private Integer id;
	private Integer userId;
	private String password;
	private String name;
	private String email;
	private String phone;
	private String role;
	private String status;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}
