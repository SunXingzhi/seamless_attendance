package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class attendanceStatistics {
        private Integer id;
        private LocalDateTime date;
        private Integer studio_id;
        private Integer total_attendance;
        private Integer total_absent;
        private Integer total_late;
        private Integer total_early_leave;
        private Integer total_leave;
        private Integer total_other;
        private LocalDateTime create_time;
        private LocalDateTime update_time;
}
