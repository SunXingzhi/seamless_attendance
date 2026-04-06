package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class userStatus {
        Integer id;
        String userNumber; // 用户工号
        String currentStatus; // 当前状态：active(在线), absent(离线)
        String lastActiveTime; // 最后在线时间，格式：yyyy-MM-dd HH:mm:ss
        String lastAbsentTime; // 最后离线时间，格式：yyyy-MM-dd HH:mm:ss
        Double todayWorkHours; // 今日工作小时数
        String checkInTime; // 今日打卡时间，格式：HH:mm:ss
        String checkOutTime; // 今日下班时间，格式：HH:mm:ss
        String createTime;
        String updateTime;
}