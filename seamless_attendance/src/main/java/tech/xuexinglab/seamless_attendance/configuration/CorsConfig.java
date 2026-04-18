package tech.xuexinglab.seamless_attendance.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CorsConfig - 全局CORS配置类
 * 功能：
 * 1. 配置全局跨域资源共享策略
 * 2. 允许前端开发服务器和生产环境访问
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

        @Override
        public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/seamless_attendance/api/**")
                                .allowedOriginPatterns("*")
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedHeaders("*")
                                .allowCredentials(true)
                                .maxAge(3600);
        }
}