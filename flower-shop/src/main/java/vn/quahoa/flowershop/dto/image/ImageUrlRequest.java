package vn.quahoa.flowershop.dto.image;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ImageUrlRequest {
  @NotBlank
  private String imageUrl;
}
