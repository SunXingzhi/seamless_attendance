package tech.xuexinglab.seamless_attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class user {
	private Integer id;
	private String user_number;
	private String user_name;
	private String name;
	private String password;
        private String contact_type;
	private String contact_value;
        private String role;
        private String job_title;
        private String work_content;
	private String studio_id;
	private Integer studio_admin_id;
        private String avatar;          // 头像URL
	private String status;          // active:在勤, absent:缺勤, excused:请假, holiday:放假, leave:离开
        private String pairing_status;
        private Integer device_id;
        private String join_date;
	private String create_time;
	private String update_time;
}
