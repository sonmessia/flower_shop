package vn.quahoa.flowershop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageUrlRequest {
    @NotBlank(message = "Image URL must not be blank")
    private String imageUrl;
}
