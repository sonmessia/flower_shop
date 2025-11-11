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
import vn.quahoa.flowershop.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageStorageService imageStorageService;

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
        product.setImageUrl(request.getImageUrl()); // Temporary, will be processed after save
        product.setCategory(category);
        
        // Save product first to get ID
        product = productRepository.save(product);
        
        // Download and save main image if URL is provided
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            try {
                String localImageUrl = imageStorageService.saveImageFromUrl(
                    request.getImageUrl(), product.getId(), true);
                product.setImageUrl(localImageUrl);
                product = productRepository.save(product);
            } catch (IOException e) {
                // Keep original URL if download fails
                product.setImageUrl(request.getImageUrl());
            }
        }
        
        // Add multiple images if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int order = 0;
            for (String imageUrl : request.getImageUrls()) {
                try {
                    String localImageUrl = imageStorageService.saveImageFromUrl(
                        imageUrl, product.getId(), false);
                    
                    ProductImage image = new ProductImage();
                    image.setImageUrl(localImageUrl);
                    image.setDisplayOrder(order++);
                    image.setProduct(product);
                    product.getImages().add(image);
                } catch (IOException e) {
                    // Skip if download fails, just log it
                    // Continue to next image
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
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
        
        // Update images if provided
        if (request.getImageUrls() != null) {
            product.getImages().clear();
            int order = 0;
            for (String imageUrl : request.getImageUrls()) {
                ProductImage image = new ProductImage();
                image.setImageUrl(imageUrl);
                image.setDisplayOrder(order++);
                image.setProduct(product);
                product.getImages().add(image);
            }
        }
        
        return productRepository.save(product);
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

    public String uploadProductImage(Long productId, MultipartFile file) {
        Product product = getById(productId);

        try {
            String imageUrl = imageStorageService.saveImageFromFile(file, productId, false);
            
            ProductImage productImage = new ProductImage();
            productImage.setFileName(file.getOriginalFilename());
            productImage.setFilePath(imageUrl);
            productImage.setImageUrl(imageUrl);
            productImage.setProduct(product);
            product.getImages().add(productImage);

            productRepository.save(product);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public String uploadMainProductImage(Long productId, MultipartFile file) {
        Product product = getById(productId);

        try {
            String imageUrl = imageStorageService.saveImageFromFile(file, productId, true);
            
            // Update product's main image URL
            product.setImageUrl(imageUrl);
            productRepository.save(product);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload main image", e);
        }
    }
}
