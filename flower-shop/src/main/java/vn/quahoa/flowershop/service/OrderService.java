package vn.quahoa.flowershop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.quahoa.flowershop.dto.order.OrderCheckoutRequest;
import vn.quahoa.flowershop.dto.order.OrderItemResponse;
import vn.quahoa.flowershop.dto.order.OrderResponse;
import vn.quahoa.flowershop.exception.ResourceNotFoundException;
import vn.quahoa.flowershop.exception.ValidationException;
import vn.quahoa.flowershop.model.Admin;
import vn.quahoa.flowershop.model.CartItem;
import vn.quahoa.flowershop.model.Order;
import vn.quahoa.flowershop.model.OrderItem;
import vn.quahoa.flowershop.model.OrderStatus;
import vn.quahoa.flowershop.model.User;
import vn.quahoa.flowershop.repository.CartItemRepository;
import vn.quahoa.flowershop.repository.OrderRepository;
import vn.quahoa.flowershop.repository.UserRepository;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    public OrderResponse checkout(Long userId, OrderCheckoutRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<CartItem> cartItems = cartItemRepository.findByUser_IdOrderByCreatedAtAsc(userId);
        if (cartItems.isEmpty()) {
            throw new ValidationException("cart", "Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingName(request.getShippingName());
        order.setShippingPhone(request.getShippingPhone());
        order.setShippingStreet(request.getShippingStreet());
        order.setShippingCity(request.getShippingCity());
        order.setShippingPostalCode(request.getShippingPostalCode());
        order.setNote(request.getNote());

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            BigDecimal unitPrice = BigDecimal.valueOf(cartItem.getProduct().getPrice()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(lineTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            order.addItem(orderItem);
        }

        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

        Order savedOrder = orderRepository.save(order);
        cartItemRepository.deleteByUser_Id(userId);

        return toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUser_IdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toOrderResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toOrderResponse)
            .toList();
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status, String cancelReason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (status == OrderStatus.CANCELLED) {
            if (cancelReason == null || cancelReason.trim().isEmpty()) {
                throw new ValidationException("cancelReason", "cancelReason is required when cancelling order");
            }
            order.setCancellationMessage(cancelReason.trim());
            order.setCancellationBy("ADMIN:" + resolveCancellationActor("SYSTEM"));
        }

        order.setStatus(status);
        Order updated = orderRepository.save(order);
        return toOrderResponse(updated);
    }

    public OrderResponse cancelMyOrder(Long userId, Long orderId, String cancelReason, String cancelledBy) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!canUserCancel(order.getStatus())) {
            throw new ValidationException("status", "Order cannot be cancelled once processing has started");
        }

        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            throw new ValidationException("cancelReason", "cancelReason is required");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationMessage("Customer cancellation reason: " + cancelReason.trim());
        String actor = cancelledBy != null && !cancelledBy.isBlank() ? cancelledBy : "CUSTOMER";
        order.setCancellationBy("USER:" + actor);

        Order updated = orderRepository.save(order);
        return toOrderResponse(updated);
    }

    private boolean canUserCancel(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    private String resolveCancellationActor(String fallback) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return fallback;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Admin admin && admin.getUsername() != null && !admin.getUsername().isBlank()) {
            return admin.getUsername();
        }
        if (principal instanceof User user) {
            if (user.getFullName() != null && !user.getFullName().isBlank()) {
                return user.getFullName();
            }
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                return user.getEmail();
            }
        }
        return fallback;
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
            .map(item -> {
                BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

                return OrderItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .productCode(item.getProduct().getProductCode())
                    .productName(item.getProduct().getName())
                    .imageUrl(item.getProduct().getImageUrl())
                    .unitPrice(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .lineTotal(lineTotal)
                    .build();
            })
            .toList();

        int totalItems = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();

        return OrderResponse.builder()
            .id(order.getId())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .totalItems(totalItems)
            .shippingName(order.getShippingName())
            .shippingPhone(order.getShippingPhone())
            .shippingStreet(order.getShippingStreet())
            .shippingCity(order.getShippingCity())
            .shippingPostalCode(order.getShippingPostalCode())
            .note(order.getNote())
            .cancellationMessage(order.getCancellationMessage())
            .cancellationBy(order.getCancellationBy())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(itemResponses)
            .build();
    }
}
