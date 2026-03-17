package _2280600764_NguyenTruongGiang.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import _2280600764_NguyenTruongGiang.model.OrderStatus;

@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "status is required")
    private OrderStatus status;

    private String cancelReason;

    private String adminMessage;
}
