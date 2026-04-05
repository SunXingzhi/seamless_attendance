package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudioYearlyStats {
    private Integer id;
    private Long studioId; // 工作室ID
    private Integer year; // 年份
    private Integer totalUsers; // 总用户数
    private Double averageAttendanceRate; // 平均出勤率(百分比)
    private Double averageActivityRate; // 平均活跃率(百分比)
    private Integer totalLateCount; // 总迟到次数
    private Integer totalEarlyLeaveCount; // 总早退次数
    private Integer totalAbsentCount; // 总缺勤次数
    private Integer totalLeaveDays; // 总请假天数
    private Double totalOvertimeHours; // 总加班小时数
    private Double performanceScore; // 绩效评分(0-100)
    private String createTime; // 创建时间 (yyyy-MM-dd HH:mm:ss)
    private String updateTime; // 更新时间 (yyyy-MM-dd HH:mm:ss)
}