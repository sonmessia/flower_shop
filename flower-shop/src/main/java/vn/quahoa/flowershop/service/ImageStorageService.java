package vn.quahoa.flowershop.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageStorageService {

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.storage.local-path:/app/images}")
    private String localStoragePath;

    private static final String UPLOAD_DIR_PRODUCTS = "products";
    private static final String UPLOAD_DIR_BLOGS = "blogs";

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Save image from MultipartFile for products
     */
    public String saveImageFromFile(MultipartFile file, Long productId, boolean isMain) throws IOException {
        return saveImageFromFileWithType(file, productId, isMain, "product");
    }

    /**
     * Save image from MultipartFile with type (product/blog)
     */
    private String saveImageFromFileWithType(MultipartFile file, Long entityId, boolean isMain, String type) throws IOException {
        String baseDir = type.equals("blog") ? UPLOAD_DIR_BLOGS : UPLOAD_DIR_PRODUCTS;
        String subDir = isMain ? "/main" : "";
        String relativePathFromImages = baseDir + "/" + entityId + subDir;
        
        // Absolute path on disk: /app/images/blogs/1/main
        Path uploadDir = Paths.get(localStoragePath, relativePathFromImages);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String prefix = isMain ? "main_" : "";
        String fileName = prefix + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Saved {} image from file: {}", type, filePath);

        // Return URL: http://localhost:8080/images/blogs/1/main/main_123.jpg
        return baseUrl + "/images/" + relativePathFromImages + "/" + fileName;
    }

    /**
     * Save image from URL for products
     */
    public String saveImageFromUrl(String imageUrl, Long productId, boolean isMain) throws IOException {
        return saveImageFromUrlWithType(imageUrl, productId, isMain, "product");
    }

    /**
     * Save image from file for blogs
     */
    public String saveBlogImageFromFile(MultipartFile file, Long blogId, boolean isMain) throws IOException {
        return saveImageFromFileWithType(file, blogId, isMain, "blog");
    }

    /**
     * Save image from URL for blogs
     */
    public String saveBlogImageFromUrl(String imageUrl, Long blogId, boolean isMain) throws IOException {
        return saveImageFromUrlWithType(imageUrl, blogId, isMain, "blog");
    }

    /**
     * Download and save image from URL with type (product/blog)
     */
    private String saveImageFromUrlWithType(String imageUrl, Long entityId, boolean isMain, String type) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        // If it's already a local URL, return as is
        if (imageUrl.startsWith(baseUrl)) {
            return imageUrl;
        }

        String baseDir = type.equals("blog") ? UPLOAD_DIR_BLOGS : UPLOAD_DIR_PRODUCTS;
        String subDir = isMain ? "/main" : "";
        String relativePathFromImages = baseDir + "/" + entityId + subDir;
        
        // Absolute path on disk: /app/images/blogs/1/main
        Path uploadDir = Paths.get(localStoragePath, relativePathFromImages);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Extract file extension from URL
        String extension = getExtensionFromUrl(imageUrl);
        String prefix = isMain ? "main_" : "";
        String fileName = prefix + System.currentTimeMillis() + extension;
        Path filePath = uploadDir.resolve(fileName);

        // Download image from URL
        try (InputStream in = new URL(imageUrl).openStream()) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Downloaded {} image from URL: {} to {}", type, imageUrl, filePath);
        } catch (Exception e) {
            log.error("Failed to download {} image from URL: {}", type, imageUrl, e);
            // If download fails, return original URL
            return imageUrl;
        }

        // Return URL: http://localhost:8080/images/blogs/1/main/main_123.jpg
        return baseUrl + "/images/" + relativePathFromImages + "/" + fileName;
    }

    /**
     * Extract file extension from URL
     */
    private String getExtensionFromUrl(String url) {
        try {
            String path = new URL(url).getPath();
            int lastDot = path.lastIndexOf('.');
            if (lastDot > 0) {
                return path.substring(lastDot);
            }
        } catch (Exception e) {
            log.warn("Could not extract extension from URL: {}", url);
        }
        return ".jpg"; // default extension
    }

    /**
     * Download image from URL and return as byte array
     */
    public byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to download image from URL: {}", imageUrl, e);
            throw new IOException("Failed to download image from URL: " + imageUrl, e);
        }
    }

    /**
     * Delete image file
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(baseUrl)) {
            return;
        }

        try {
            // URL: http://localhost:8080/images/blogs/1/main/main_123.jpg
            // Extract: blogs/1/main/main_123.jpg
            String relativePath = imageUrl.replace(baseUrl + "/images/", "");
            
            // Full path: /app/images/blogs/1/main/main_123.jpg
            Path filePath = Paths.get(localStoragePath, relativePath);
            Files.deleteIfExists(filePath);
            log.info("Deleted image: {}", filePath);
        } catch (Exception e) {
            log.error("Failed to delete image: {}", imageUrl, e);
        }
    }
}
