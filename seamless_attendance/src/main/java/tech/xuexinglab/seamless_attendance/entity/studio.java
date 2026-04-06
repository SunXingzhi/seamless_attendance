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
	private String studioName;
	private String studioCode;
	private String description;
	private String personnels;
	private Integer adminId;
	private String adminName;
	private String adminUserNumber;
	private Integer memberCount;
	private Integer maxMemberCount;
	private String status;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}
