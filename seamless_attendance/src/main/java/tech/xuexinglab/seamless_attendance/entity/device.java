package tech.xuexinglab.seamless_attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class device {
        private Integer id;
        private String device_name;
        private String device_id;
        private String studio_codes;
        private String personnels;
        private String status;
        private String last_active_time;
        private LocalDateTime create_time;
        private LocalDateTime update_time;
}
