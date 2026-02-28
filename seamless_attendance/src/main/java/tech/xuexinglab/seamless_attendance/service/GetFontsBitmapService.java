package tech.xuexinglab.seamless_attendance.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import tech.xuexinglab.seamless_attendance.entity.device;

import tech.xuexinglab.seamless_attendance.DTO.deviceDTO;
@Service
public class GetFontsBitmapService {
        public String getFontsBitmap(String fonts_name) {
                RestTemplate restTemplate = new RestTemplate();
                String url = "http://127.0.0.1:5000/getFontsBitmap?fonts_name=" + fonts_name;
                String requestBody = "{\"key\":\"value\"}";
                ResponseEntity<String> postResponse = restTemplate.postForEntity(url, requestBody, String.class);
                System.out.println(postResponse.getBody());
                return postResponse.getBody().replace("\\n", "\n");
        }


        public String getFontsBitmapFromUser(deviceDTO device){
                RestTemplate restTemplate = new RestTemplate();
                String url = "http://127.0.0.1:5000/getFontsBitmapFromUser";
                String requestBody = "{\"text\":\"test\"}";
                ResponseEntity<String> postResponse = restTemplate.postForEntity(url, requestBody, String.class);
                System.out.println(postResponse.getBody());
                return postResponse.getBody().replace("\\n", "\n");
        }
}
