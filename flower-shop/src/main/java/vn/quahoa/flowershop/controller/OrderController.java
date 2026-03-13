package vn.quahoa.flowershop.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.quahoa.flowershop.dto.order.OrderCheckoutRequest;
import vn.quahoa.flowershop.dto.order.OrderResponse;
import vn.quahoa.flowershop.dto.order.UserCancelOrderRequest;
import vn.quahoa.flowershop.model.User;
import vn.quahoa.flowershop.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@AuthenticationPrincipal User user,
                                  @Valid @RequestBody OrderCheckoutRequest request) {
        return orderService.checkout(user.getId(), request);
    }

    @GetMapping("/me")
    public List<OrderResponse> getMyOrders(@AuthenticationPrincipal User user) {
        return orderService.getMyOrders(user.getId());
    }

    @GetMapping("/me/{orderId}")
    public OrderResponse getMyOrderById(@AuthenticationPrincipal User user,
                                        @PathVariable Long orderId) {
        return orderService.getMyOrderById(user.getId(), orderId);
    }

    @PutMapping("/me/{orderId}/cancel")
    public OrderResponse cancelMyOrder(@AuthenticationPrincipal User user,
                                       @PathVariable Long orderId,
                                       @Valid @RequestBody UserCancelOrderRequest request) {
        String cancelledBy = user.getFullName() != null && !user.getFullName().isBlank()
            ? user.getFullName()
            : user.getEmail();
        return orderService.cancelMyOrder(user.getId(), orderId, request.getCancelReason(), cancelledBy);
    }

    @PostMapping("/me/{orderId}/cancel")
    public OrderResponse cancelMyOrderPost(@AuthenticationPrincipal User user,
                                           @PathVariable Long orderId,
                                           @Valid @RequestBody UserCancelOrderRequest request) {
        String cancelledBy = user.getFullName() != null && !user.getFullName().isBlank()
            ? user.getFullName()
            : user.getEmail();
        return orderService.cancelMyOrder(user.getId(), orderId, request.getCancelReason(), cancelledBy);
    }
}
