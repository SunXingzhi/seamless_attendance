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
        private Integer studioId;
        private Integer totalAttendance;
        private Integer totalAbsent;
        private Integer totalLate;
        private Integer totalEarlyLeave;
        private Integer totalLeave;
        private Integer totalOther;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
}
