package vn.quahoa.flowershop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.quahoa.flowershop.dto.order.OrderCheckoutRequest;
import vn.quahoa.flowershop.dto.order.OrderItemResponse;
import vn.quahoa.flowershop.dto.order.OrderResponse;
import vn.quahoa.flowershop.exception.ResourceNotFoundException;
import vn.quahoa.flowershop.exception.ValidationException;
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

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        order.setStatus(status);
        Order updated = orderRepository.save(order);
        return toOrderResponse(updated);
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
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(itemResponses)
            .build();
    }
}
