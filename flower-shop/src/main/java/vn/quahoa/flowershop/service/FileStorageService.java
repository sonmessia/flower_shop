package vn.quahoa.flowershop.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.quahoa.flowershop.config.StorageProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StorageProperties storageProperties;
    
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(storageProperties.getLocalPath());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created storage directory: {}", uploadPath.toAbsolutePath());
            }
            
            // Tạo các thư mục con cho products và blogs
            Path productsPath = uploadPath.resolve("products");
            Path blogsPath = uploadPath.resolve("blogs");
            
            if (!Files.exists(productsPath)) {
                Files.createDirectories(productsPath);
                log.info("Created products directory: {}", productsPath.toAbsolutePath());
            }
            
            if (!Files.exists(blogsPath)) {
                Files.createDirectories(blogsPath);
                log.info("Created blogs directory: {}", blogsPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory!", e);
        }
    }
    
    /**
     * Lưu file từ MultipartFile và trả về đường dẫn tương đối
     * 
     * @param file MultipartFile cần lưu
     * @param subDirectory Thư mục con (ví dụ: "products", "blogs")
     * @return Đường dẫn tương đối (ví dụ: "products/uuid_filename.jpg")
     */
    public String saveFile(MultipartFile file, String subDirectory) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot save empty file");
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileName = generateUniqueFileName(originalFileName);
        
        return saveFileLocally(file.getInputStream(), fileName, subDirectory);
    }
    
    /**
     * Lưu file từ InputStream và trả về đường dẫn tương đối
     * 
     * @param inputStream InputStream của file
     * @param originalFileName Tên file gốc
     * @param subDirectory Thư mục con (ví dụ: "products", "blogs")
     * @return Đường dẫn tương đối (ví dụ: "products/uuid_filename.jpg")
     */
    public String saveFileLocally(InputStream inputStream, String originalFileName, String subDirectory) throws IOException {
        String fileName = generateUniqueFileName(originalFileName);
        
        // Tạo đường dẫn đầy đủ
        Path uploadPath = Paths.get(storageProperties.getLocalPath()).resolve(subDirectory);
        
        // Đảm bảo thư mục tồn tại
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        
        // Copy file vào thư mục
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Saved file: {}", filePath.toAbsolutePath());
        
        // Trả về đường dẫn tương đối
        return subDirectory + "/" + fileName;
    }
    
    /**
     * Tải file từ URL và lưu vào local storage
     * 
     * @param imageUrl URL của ảnh cần tải
     * @param subDirectory Thư mục con (ví dụ: "products", "blogs")
     * @return Đường dẫn tương đối
     */
    public String saveFileFromUrl(String imageUrl, String subDirectory) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        
        log.info("Downloading image from URL: {}", imageUrl);
        
        try {
            java.net.URI uri = java.net.URI.create(imageUrl);
            java.net.URL url = uri.toURL();
            
            // Mở connection với timeout và User-Agent header
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setInstanceFollowRedirects(true);
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                String fileName = extractFileNameFromUrl(imageUrl);
                
                try (InputStream inputStream = connection.getInputStream()) {
                    String relativePath = saveFileLocally(inputStream, fileName, subDirectory);
                    log.info("Successfully downloaded and saved image from URL: {}", imageUrl);
                    return relativePath;
                }
            } else {
                String errorMsg = String.format("Failed to download image from URL: %s, HTTP code: %d", imageUrl, responseCode);
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = String.format("Error downloading image from URL: %s, Error: %s", imageUrl, e.getMessage());
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        }
    }
    
    /**
     * Xóa file khỏi storage
     * 
     * @param relativePath Đường dẫn tương đối của file cần xóa
     */
    public void deleteFile(String relativePath) throws IOException {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return;
        }
        
        Path filePath = Paths.get(storageProperties.getLocalPath()).resolve(relativePath);
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Deleted file: {}", filePath.toAbsolutePath());
        }
    }
    
    /**
     * Tạo tên file duy nhất bằng UUID
     * 
     * @param originalFileName Tên file gốc
     * @return Tên file mới với UUID
     */
    private String generateUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        return uuid + extension;
    }
    
    /**
     * Trích xuất tên file từ URL
     * 
     * @param url URL cần trích xuất
     * @return Tên file
     */
    private String extractFileNameFromUrl(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        
        // Nếu URL có query string, loại bỏ nó
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        
        // Nếu không có extension, thêm .jpg mặc định
        if (!fileName.contains(".")) {
            fileName += ".jpg";
        }
        
        return fileName;
    }
    
    /**
     * Tạo URL công khai để truy cập file
     * 
     * @param relativePath Đường dẫn tương đối
     * @return URL công khai
     */
    public String getPublicUrl(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return storageProperties.getBaseUrl();
        }
        
        // Đảm bảo không có double slashes
        String baseUrl = storageProperties.getBaseUrl();
        if (baseUrl.endsWith("/") && relativePath.startsWith("/")) {
            return baseUrl + relativePath.substring(1);
        } else if (!baseUrl.endsWith("/") && !relativePath.startsWith("/")) {
            return baseUrl + "/" + relativePath;
        }
        
        return baseUrl + relativePath;
    }
}
