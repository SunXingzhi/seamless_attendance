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
	private Integer user_id;
	private String password;
	private String name;
	private String email;
	private String phone;
	private String role;
	private String status;
	private LocalDateTime create_time;
	private LocalDateTime update_time;
}
