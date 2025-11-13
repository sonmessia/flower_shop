package vn.quahoa.flowershop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {
    
    /**
     * Đường dẫn tuyệt đối nơi lưu trữ file trên host/container
     * Ví dụ: /app/images
     */
    private String localPath;
    
    /**
     * Base URL công khai để truy cập ảnh
     * Ví dụ: http://localhost:8080/images
     */
    private String baseUrl;
}
