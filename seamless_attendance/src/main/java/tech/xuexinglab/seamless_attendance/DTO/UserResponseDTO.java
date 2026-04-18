package tech.xuexinglab.seamless_attendance.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String userNumber;
    private String userName;
    private String name;
    private String contactType;
    private String contactValue;
    private String role;
    private String jobTitle;
    private String workContent;
    private String studioId;
    private Integer studioAdminId;
    private String avatar;          // 头像URL
    private String status;          // active:在勤, absent:缺勤, excused:请假, holiday:放假, leave:离开
    private String pairingStatus;
    private Integer deviceId;
    private String joinDate;
    private String createTime;
    private String updateTime;
}
