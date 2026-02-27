package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class userStatus {
        Integer id;
        String user_number; // 用户工号
        String current_status; // 当前状态：active(在线), absent(离线)
        String last_active_time; // 最后在线时间，格式：yyyy-MM-dd HH:mm:ss
        String last_absent_time; // 最后离线时间，格式：yyyy-MM-dd HH:mm:ss
        Double today_work_hours; // 今日工作小时数
        String check_in_time; // 今日打卡时间，格式：HH:mm:ss
        String check_out_time; // 今日下班时间，格式：HH:mm:ss
        String create_time;
        String update_time;
}