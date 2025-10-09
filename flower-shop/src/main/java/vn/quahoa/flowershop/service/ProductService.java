package vn.quahoa.flowershop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.quahoa.flowershop.dto.product.ProductCreateRequest;
import vn.quahoa.flowershop.dto.product.ProductUpdateRequest;
import vn.quahoa.flowershop.exception.ResourceNotFoundException;
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

    public Product createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Product product = new Product();
        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
        
        // Add multiple images if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
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

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public List<Product> getByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
        return productRepository.findByCategory_Id(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    public Product updateProduct(Long id, ProductUpdateRequest request) {
        Product product = getById(id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

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
        productRepository.delete(product);
    }
}
