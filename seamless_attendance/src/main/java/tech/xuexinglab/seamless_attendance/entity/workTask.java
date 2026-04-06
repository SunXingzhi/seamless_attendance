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
        private String taskName;
        private String taskType;
        private Integer studioId;
        private Integer priority;
        private String taskDescription;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer estimatedTime;
        private Integer actualTime;
        private String status; // 0:未开始, 1:进行中, 2:已完成, 3:已取消
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

}
