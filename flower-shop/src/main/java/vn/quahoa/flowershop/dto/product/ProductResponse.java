package vn.quahoa.flowershop.dto.product;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import vn.quahoa.flowershop.model.Product;

@Data
public class ProductResponse {
    private Long id;
    private String productCode;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private List<String> imageUrls;
    private Long categoryId;
    private String categoryName;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setProductCode(product.getProductCode());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setImageUrl(product.getImageUrl());
        response.setCategoryId(product.getCategoryId());
        
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }
        
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            response.setImageUrls(
                product.getImages().stream()
                    .map(img -> img.getImageUrl())
                    .collect(Collectors.toList())
            );
        }
        
        return response;
    }
}
