package _2280600764_NguyenTruongGiang.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCancelOrderRequest {

    @NotBlank(message = "cancelReason is required")
    private String cancelReason;
}
