package tech.xuexinglab.seamless_attendance.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MQTTMessageDTO {
	private String device_name;
    private String device_user_number;
    private String user_number;
    private Integer device_id;
	private String topic;
	private String payload;
	private Integer qos;
}
