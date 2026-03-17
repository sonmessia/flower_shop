package _2280600764_NguyenTruongGiang.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

  @NotBlank(message = "Name is required")
  private String name;
}
