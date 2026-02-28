package tech.xuexinglab.seamless_attendance.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import tech.xuexinglab.seamless_attendance.service.utilitySevice;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class deviceDTO {

	@NotBlank(message = "设备名称不能为空")
	private String device_name;

	// 原始设备名称(A1, A2 为具体的工作设备,那么A就是源设备名称/ID)
	private String origin_device_name;

	
	@NotBlank(message = "设备ID不能为空")
	private String device_id;

	private String type;

	private String location;

	private String studio_codes;

	private String personnels;

	public String getOriginDeviceName() {
		if (this.device_name != null && this.device_name.length() > 0) {
			// 简单的字符串处理：提取字母部分（如从 "A1" 提取 "A"）
			String deviceName = this.device_name.trim();
			StringBuilder result = new StringBuilder();
			
			for (int i = 0; i < deviceName.length(); i++) {
				char c = deviceName.charAt(i);
				if (Character.isLetter(c)) {
					result.append(c);
				} else {
					// 遇到第一个非字母字符时停止
					break;
				}
			}
			
			return result.length() > 0 ? result.toString() : null;
		}
		return null;
	}

	public int getDeviceIndex() {
		if (this.device_id != null && this.device_id.length() > 0) {
			// 简单的字符串处理：提取数字部分（如从 "A1" 提取 "1"）
			String deviceId = this.device_id.trim();
			StringBuilder result = new StringBuilder();
			boolean foundNumber = false;
			
			for (int i = 0; i < deviceId.length(); i++) {
				char c = deviceId.charAt(i);
				if (Character.isDigit(c)) {
					result.append(c);
					foundNumber = true;
				} else if (foundNumber) {
					// 已经找到数字，遇到非数字字符时停止
					break;
				}
			}
			
			return result.length() > 0 ? Integer.parseInt(result.toString()) : 1;
		}
		return 1;
	}

	
}
