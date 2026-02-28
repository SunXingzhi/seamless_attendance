package tech.xuexinglab.seamless_attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import tech.xuexinglab.seamless_attendance.service.utilitySevice;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class device {
        private Integer id;
        private String device_name;
        private String origin_device_name;
        private String device_id;
        private String studio_codes;
        private String personnels;
        private String status;
        private String last_active_time;
        private LocalDateTime create_time;
        private LocalDateTime update_time;
        
        public String getOriginDeviceName(){
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

        public void setOriginDeviceName(String origin_device_name){
                this.origin_device_name = origin_device_name;
        }

}
