package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyStats {
        private Integer id;
        private String userNumber; // 用户工号
        private String date; // 统计日期 (yyyy-MM-dd)
        private Double workHours; // 工作时长(小时)
        private Double activityScore; // 活跃度评分(0-100)
        private String attendanceStatus; // 出勤状态：active(在勤), late(迟到), leave(离开), absent(缺勤), holiday(假期), excused(请假)
        private String checkInTime; // 上班打卡时间 (HH:mm:ss)
        private String checkOutTime; // 下班打卡时间 (HH:mm:ss)
        private Integer lateMinutes; // 迟到分钟数
        private Integer earlyLeaveMinutes; // 早退分钟数
        private String createTime; // 创建时间 (yyyy-MM-dd HH:mm:ss)
        private String updateTime; // 更新时间 (yyyy-MM-dd HH:mm:ss)
}