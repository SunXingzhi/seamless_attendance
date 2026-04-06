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
        private String deviceName;
        private String originDeviceName;
        private String deviceId;
        private String studioCodes;
        private String personnels;
        private String status;
        private String lastActiveTime;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        
        public String getOriginDeviceName(){
                if (this.deviceName != null && this.deviceName.length() > 0) {
                        // 简单的字符串处理：提取字母部分（如从 "A1" 提取 "A"）
                        String deviceName = this.deviceName.trim();
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

        public void setOriginDeviceName(String originDeviceName){
                this.originDeviceName = originDeviceName;
        }

}
