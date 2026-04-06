package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class attendanceRecord {
        Integer id;
        String userNumber; // 用户工号
        String date; // 日期，格式：yyyy-MM-dd
        String checkInTime; // 打卡时间，格式：HH:mm:ss
        String checkOutTime; // 下班时间，格式：HH:mm:ss
        Double workHours; // 工作小时数
        String status; // 状态：active(在勤), late(迟到), leave(离开), absent(缺勤), holiday(假期), excused(请假)
        String createTime;
        String updateTime;
}