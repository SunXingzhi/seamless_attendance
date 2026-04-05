package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWeeklyStats {
    private Integer id;
    private String userNumber; // 用户工号
    private String weekStartDate; // 周开始日期 (yyyy-MM-dd)
    private String weekEndDate; // 周结束日期 (yyyy-MM-dd)
    private Integer totalWorkDays; // 总工作天数
    private Double totalWorkHours; // 总工作时长(小时)
    private Double attendanceRate; // 出勤率(百分比)
    private Double activityScore; // 活跃度评分(0-100)
    private Integer lateCount; // 迟到次数
    private Integer earlyLeaveCount; // 早退次数
    private Integer absentCount; // 缺勤次数
    private String createTime; // 创建时间 (yyyy-MM-dd HH:mm:ss)
    private String updateTime; // 更新时间 (yyyy-MM-dd HH:mm:ss)
}