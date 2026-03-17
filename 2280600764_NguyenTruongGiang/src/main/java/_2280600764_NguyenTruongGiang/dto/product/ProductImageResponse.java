package _2280600764_NguyenTruongGiang.dto.product;

import lombok.Data;
import _2280600764_NguyenTruongGiang.model.ProductImage;

@Data
public class ProductImageResponse {
  private Long id;
  private String imageUrl;
  private String fileName;
  private Integer displayOrder;

  public static ProductImageResponse fromEntity(ProductImage image) {
    ProductImageResponse response = new ProductImageResponse();
    response.setId(image.getId());
    response.setImageUrl(image.getImageUrl());
    response.setFileName(image.getFileName());
    response.setDisplayOrder(image.getDisplayOrder());
    return response;
  }
}
