package _2280600764_NguyenTruongGiang.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import _2280600764_NguyenTruongGiang.dto.order.OrderResponse;
import _2280600764_NguyenTruongGiang.dto.order.OrderStatusUpdateRequest;
import _2280600764_NguyenTruongGiang.service.OrderService;

@RestController
@RequestMapping("/api/admins/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable Long orderId,
                                      @Valid @RequestBody OrderStatusUpdateRequest request) {
        String cancelReason = request.getCancelReason();
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            cancelReason = request.getAdminMessage();
        }
        return orderService.updateOrderStatus(orderId, request.getStatus(), cancelReason);
    }
}
