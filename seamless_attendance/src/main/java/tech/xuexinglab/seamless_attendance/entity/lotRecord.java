package tech.xuexinglab.seamless_attendance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class lotRecord {
        Integer id;
        String  device_id;
        String personnel1_status;
        String personnel2_status;
        String personnel3_status;
        
}
