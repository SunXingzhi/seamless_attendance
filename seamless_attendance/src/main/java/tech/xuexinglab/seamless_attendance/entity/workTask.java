package tech.xuexinglab.seamless_attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class workTask {
        private Integer id;
        private String task_name;
        private String task_type;
        private Integer studio_id;
        private Integer priority;
        private String task_description;
        private LocalDateTime start_date;
        private LocalDateTime end_date;
        private Integer estimated_time;
        private Integer actual_time;
        private String status; // 0:未开始, 1:进行中, 2:已完成, 3:已取消
        private LocalDateTime create_time;
        private LocalDateTime update_time;

}
