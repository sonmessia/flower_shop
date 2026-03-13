package vn.quahoa.flowershop.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.quahoa.flowershop.model.OrderStatus;

@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "status is required")
    private OrderStatus status;
}
