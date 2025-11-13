package vn.quahoa.flowershop.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "products")
@Data
@ToString(exclude = "category")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;
    
    private double price;
    
    // Main product image - stored as file path
    @Column(name = "main_image_url", length = 500)
    private String mainImageUrl;

    // Additional product images - stored in separate table
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @JsonProperty(value = "categoryId", access = JsonProperty.Access.READ_ONLY)
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }
    
    /**
     * Returns the URL to access the main product image
     * The image path is stored in the database and served via static resource handler
     */
    @JsonProperty(value = "imageUrl", access = JsonProperty.Access.READ_ONLY)
    public String getImageUrl() {
        return mainImageUrl;
    }
    
    /**
     * Helper method to add an additional image to the product
     */
    public void addImage(ProductImage productImage) {
        images.add(productImage);
        productImage.setProduct(this);
    }
    
    /**
     * Helper method to remove an additional image from the product
     */
    public void removeImage(ProductImage productImage) {
        images.remove(productImage);
        productImage.setProduct(null);
    }
}

