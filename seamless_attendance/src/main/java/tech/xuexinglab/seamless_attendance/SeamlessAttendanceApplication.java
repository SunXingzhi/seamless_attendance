package tech.xuexinglab.seamless_attendance;

import org.mybatis.spring.annotation.MapperScan; // 暂时注释，待引入 mybatis-spring 依赖后恢复
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("tech.xuexinglab.seamless_attendance.mapper")
public class SeamlessAttendanceApplication {

	public static void main(String[] args) {
                SpringApplication.run(SeamlessAttendanceApplication.class, args);
        }

}
