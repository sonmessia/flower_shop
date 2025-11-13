package vn.quahoa.flowershop.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.quahoa.flowershop.dto.product.ProductCreateRequest;
import vn.quahoa.flowershop.dto.product.ProductUpdateRequest;
import vn.quahoa.flowershop.exception.ResourceNotFoundException;
import vn.quahoa.flowershop.exception.ValidationException;
import vn.quahoa.flowershop.model.Category;
import vn.quahoa.flowershop.model.Product;
import vn.quahoa.flowershop.model.ProductImage;
import vn.quahoa.flowershop.repository.CategoryRepository;
import vn.quahoa.flowershop.repository.ProductImageRepository;
import vn.quahoa.flowershop.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;

    public Product createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(Objects.requireNonNull(request.getCategoryId(), "Category ID must not be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        // Validate unique product code
        validateUniqueProductCode(request.getProductCode(), null);
        
        // Validate unique product name
        validateUniqueProductName(request.getName(), null);

        Product product = new Product();
        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        
        // Set main image from URL or binary data
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            try {
                String relativePath = fileStorageService.saveFileFromUrl(request.getImageUrl(), "products");
                product.setMainImageUrl(fileStorageService.getPublicUrl(relativePath));
            } catch (IOException e) {
                throw new RuntimeException("Failed to download and save main image from URL", e);
            }
        } else if (request.getImage() != null && request.getImage().length > 0) {
            // For backward compatibility - save from binary data (from base64 or file upload)
            try {
                java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(request.getImage());
                String relativePath = fileStorageService.saveFileLocally(inputStream, "image.jpg", "products");
                product.setMainImageUrl(fileStorageService.getPublicUrl(relativePath));
            } catch (IOException e) {
                throw new RuntimeException("Failed to save main image", e);
            }
        }
        
        product.setCategory(category);
        
        // Save product first to get ID
        product = productRepository.save(product);
        
        // Add additional images from URLs or binary data
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int order = 0;
            for (String imageUrl : request.getImageUrls()) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    try {
                        String relativePath = fileStorageService.saveFileFromUrl(imageUrl, "products");
                        
                        ProductImage image = new ProductImage();
                        image.setImageUrl(fileStorageService.getPublicUrl(relativePath));
                        image.setDisplayOrder(order++);
                        product.addImage(image);
                    } catch (IOException e) {
                        // Log and continue with other images
                        System.err.println("Failed to download image from URL: " + imageUrl);
                    }
                }
            }
        } else if (request.getImages() != null && !request.getImages().isEmpty()) {
            int order = 0;
            for (byte[] imageData : request.getImages()) {
                if (imageData != null && imageData.length > 0) {
                    try {
                        java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(imageData);
                        String relativePath = fileStorageService.saveFileLocally(inputStream, "image.jpg", "products");
                        
                        ProductImage image = new ProductImage();
                        image.setImageUrl(fileStorageService.getPublicUrl(relativePath));
                        image.setDisplayOrder(order++);
                        product.addImage(image);
                    } catch (IOException e) {
                        System.err.println("Failed to save image: " + e.getMessage());
                    }
                }
            }
            product = productRepository.save(product);
        }
        
        return product;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(Objects.requireNonNull(id, "Product ID must not be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public List<Product> getByCategory(Long categoryId) {
        if (!categoryRepository.existsById(Objects.requireNonNull(categoryId, "Category ID must not be null"))) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
        return productRepository.findByCategory_Id(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    public Product updateProduct(Long id, ProductUpdateRequest request) {
        Product product = getById(id);
        Category category = categoryRepository.findById(Objects.requireNonNull(request.getCategoryId(), "Category ID must not be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        // Validate unique product code (if changed)
        validateUniqueProductCode(request.getProductCode(), id);
        
        // Validate unique product name (if changed)
        validateUniqueProductName(request.getName(), id);

        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        
        // Update main image from URL or binary data
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            try {
                // Delete old image if exists
                if (product.getMainImageUrl() != null) {
                    deleteImageFile(product.getMainImageUrl());
                }
                
                String relativePath = fileStorageService.saveFileFromUrl(request.getImageUrl(), "products");
                product.setMainImageUrl(fileStorageService.getPublicUrl(relativePath));
            } catch (IOException e) {
                throw new RuntimeException("Failed to download and save main image from URL", e);
            }
        } else if (request.getImage() != null && request.getImage().length > 0) {
            try {
                // Delete old image if exists
                if (product.getMainImageUrl() != null) {
                    deleteImageFile(product.getMainImageUrl());
                }
                
                java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(request.getImage());
                String relativePath = fileStorageService.saveFileLocally(inputStream, "image.jpg", "products");
                product.setMainImageUrl(fileStorageService.getPublicUrl(relativePath));
            } catch (IOException e) {
                throw new RuntimeException("Failed to save main image", e);
            }
        }
        
        product.setCategory(category);
        
        return productRepository.save(product);
    }
    
    /**
     * Helper method to delete image file from storage
     */
    private void deleteImageFile(String imageUrl) {
        try {
            // Extract relative path from URL
            String baseUrl = fileStorageService.getPublicUrl("");
            if (imageUrl.startsWith(baseUrl)) {
                String relativePath = imageUrl.substring(baseUrl.length());
                fileStorageService.deleteFile(relativePath);
            }
        } catch (IOException e) {
            // Log error but don't fail the operation
            System.err.println("Failed to delete old image file: " + e.getMessage());
        }
    }

    public void deleteProduct(Long id) {
        Product product = getById(id);
        productRepository.delete(Objects.requireNonNull(product, "Product must not be null"));
    }
    
    private void validateUniqueProductCode(String productCode, Long currentId) {
        productRepository.findByProductCodeIgnoreCase(productCode).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ValidationException("productCode", "Product code already exists");
            }
        });
    }
    
    private void validateUniqueProductName(String name, Long currentId) {
        productRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ValidationException("name", "Product name already exists");
            }
        });
    }

    // ============================================
    // IMAGE UPLOAD METHODS
    // ============================================

    /**
     * Upload an additional image for a product
     */
    public String uploadProductImage(Long productId, MultipartFile file) {
        Product product = getById(productId);

        try {
            String relativePath = fileStorageService.saveFile(file, "products");
            String publicUrl = fileStorageService.getPublicUrl(relativePath);
            
            ProductImage productImage = new ProductImage();
            productImage.setFileName(file.getOriginalFilename());
            productImage.setImageUrl(publicUrl);
            product.addImage(productImage);

            productRepository.save(product);

            return publicUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload product image", e);
        }
    }

    /**
     * Upload main product image
     */
    public String uploadMainProductImage(Long productId, MultipartFile file) {
        Product product = getById(productId);

        try {
            // Delete old image if exists
            if (product.getMainImageUrl() != null) {
                deleteImageFile(product.getMainImageUrl());
            }
            
            String relativePath = fileStorageService.saveFile(file, "products");
            String publicUrl = fileStorageService.getPublicUrl(relativePath);
            
            // Update product's main image
            product.setMainImageUrl(publicUrl);
            productRepository.save(product);

            return publicUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload main product image", e);
        }
    }

    // ============================================
    // DELETE IMAGE METHODS
    // ============================================

    /**
     * Delete main image of a product
     */
    public void deleteMainImage(Long productId) {
        Product product = getById(productId);
        
        // Delete image file from storage
        if (product.getMainImageUrl() != null) {
            deleteImageFile(product.getMainImageUrl());
        }
        
        // Clear main image URL from database
        product.setMainImageUrl(null);
        productRepository.save(product);
    }

    /**
     * Delete a specific additional image by ID
     */
    public void deleteProductImage(Long productId, Long imageId) {
        Product product = getById(productId);
        
        ProductImage productImage = productImageRepository.findById(Objects.requireNonNull(imageId))
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));
        
        // Verify the image belongs to this product
        if (!productImage.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Image does not belong to this product");
        }
        
        // Delete image file from storage
        if (productImage.getImageUrl() != null) {
            deleteImageFile(productImage.getImageUrl());
        }
        
        // Remove from product's images list
        product.getImages().remove(productImage);
        
        // Delete from database
        productImageRepository.delete(Objects.requireNonNull(productImage));
    }

    /**
     * Delete all additional images of a product
     */
    public void deleteAllProductImages(Long productId) {
        Product product = getById(productId);
        
        List<ProductImage> images = List.copyOf(product.getImages()); // Copy to avoid ConcurrentModificationException
        
        for (ProductImage image : images) {
            // Delete image file from storage
            if (image.getImageUrl() != null) {
                deleteImageFile(image.getImageUrl());
            }
            
            // Delete from database
            productImageRepository.delete(Objects.requireNonNull(image));
        }
        
        // Clear from product
        product.getImages().clear();
        productRepository.save(product);
    }

    /**
     * Update main image - replace existing with new one
     */
    public String updateMainImage(Long productId, MultipartFile file) {
        // Upload new image (replaces existing)
        return uploadMainProductImage(productId, file);
    }
}
