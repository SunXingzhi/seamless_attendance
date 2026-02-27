package tech.xuexinglab.seamless_attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class studio {
	private Integer id;
	private String studio_name;
	private String studio_code;
	private String description;
	private String personnels;
	private Integer admin_id;
	private String admin_name;
	private String admin_user_number;
	private Integer member_count;
	private Integer max_member_count;
	private String status;
	private LocalDateTime create_time;
	private LocalDateTime update_time;
}
