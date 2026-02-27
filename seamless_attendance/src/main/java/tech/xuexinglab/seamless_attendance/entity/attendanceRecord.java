package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class attendanceRecord {
        Integer id;
        String user_number; // 用户工号
        String date; // 日期，格式：yyyy-MM-dd
        String check_in_time; // 打卡时间，格式：HH:mm:ss
        String check_out_time; // 下班时间，格式：HH:mm:ss
        Double work_hours; // 工作小时数
        String status; // 状态：active(出勤), absent(缺勤), excused(请假), holiday(假期)
        String create_time;
        String update_time;
}