package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudioDailyStats {
    private Integer id;
    private Long studioId; // 工作室ID
    private String date; // 统计日期 (yyyy-MM-dd)
    private Integer totalUsers; // 总用户数
    private Integer presentUsers; // 出勤用户数
    private Double attendanceRate; // 出勤率(百分比)
    private Double activityRate; // 活跃率(百分比)
    private Double averageWorkHours; // 平均工作时长
    private Integer lateCount; // 迟到次数
    private Integer earlyLeaveCount; // 早退次数
    private String createTime; // 创建时间 (yyyy-MM-dd HH:mm:ss)
    private String updateTime; // 更新时间 (yyyy-MM-dd HH:mm:ss)
}