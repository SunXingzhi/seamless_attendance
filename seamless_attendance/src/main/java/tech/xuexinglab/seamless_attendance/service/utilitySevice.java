package tech.xuexinglab.seamless_attendance.service;

import org.springframework.stereotype.Service;

@Service
public class utilitySevice {
        
        public String[] separateLettersAndNumbers(String input) {
                // 提取所有字母
                String letters = input.replaceAll("[^a-zA-Z]", "");
                // 提取所有数字
                String numbers = input.replaceAll("[^0-9]", "");
                return new String[] { letters, numbers };
        }
}
