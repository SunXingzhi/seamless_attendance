package tech.xuexinglab.seamless_attendance.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MQTTMessageDTO {
        // 设备名作为设备唯一标识, device_id 作为设备的数据库ID, 但在MQTT通信中使用设备名来标识设备
        private String device_name;
        private String device_user_number;
        private String user_number;
        private Integer device_id;
        private String topic;
        private String payload;
        private Integer qos;
}
