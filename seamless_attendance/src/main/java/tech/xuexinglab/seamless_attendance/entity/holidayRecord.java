package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class holidayRecord {
        Integer id;
        String holiday_name; // 假期名称
        String holiday_date; // 假期日期，格式：yyyy-MM-dd
        String description; // 假期描述
        String create_time;
        String update_time;
}