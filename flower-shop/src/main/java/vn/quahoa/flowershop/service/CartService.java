package vn.quahoa.flowershop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.quahoa.flowershop.dto.cart.CartItemResponse;
import vn.quahoa.flowershop.dto.cart.CartItemUpsertRequest;
import vn.quahoa.flowershop.dto.cart.CartResponse;
import vn.quahoa.flowershop.dto.cart.UpdateCartItemRequest;
import vn.quahoa.flowershop.exception.ResourceNotFoundException;
import vn.quahoa.flowershop.model.CartItem;
import vn.quahoa.flowershop.model.Product;
import vn.quahoa.flowershop.model.User;
import vn.quahoa.flowershop.repository.CartItemRepository;
import vn.quahoa.flowershop.repository.ProductRepository;
import vn.quahoa.flowershop.repository.UserRepository;

@Service
@Transactional
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUser_IdOrderByCreatedAtAsc(userId);
        return toCartResponse(items);
    }

    public CartResponse addItem(Long userId, CartItemUpsertRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        CartItem item = cartItemRepository.findByUser_IdAndProduct_Id(userId, request.getProductId())
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setUser(user);
                newItem.setProduct(product);
                newItem.setQuantity(0);
                return newItem;
            });

        item.setQuantity(item.getQuantity() + request.getQuantity());
        cartItemRepository.save(item);

        return getCart(userId);
    }

    public CartResponse updateItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByIdAndUser_Id(cartItemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return getCart(userId);
    }

    public CartResponse removeItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findByIdAndUser_Id(cartItemId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));

        cartItemRepository.delete(item);
        return getCart(userId);
    }

    public void clearCart(Long userId) {
        cartItemRepository.deleteByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUser_IdOrderByCreatedAtAsc(userId);
    }

    private CartResponse toCartResponse(List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream().map(this::toCartItemResponse).toList();

        int totalItems = items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();

        BigDecimal subtotal = items.stream()
            .map(this::calculateLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        return CartResponse.builder()
            .items(itemResponses)
            .totalItems(totalItems)
            .subtotal(subtotal)
            .build();
    }

    private BigDecimal getScaledUnitPrice(Product product) {
        return BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal unitPrice = getScaledUnitPrice(item.getProduct());

        return CartItemResponse.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productCode(item.getProduct().getProductCode())
            .productName(item.getProduct().getName())
            .imageUrl(item.getProduct().getImageUrl())
            .unitPrice(unitPrice)
            .quantity(item.getQuantity())
            .lineTotal(calculateLineTotal(item))
            .build();
    }

    private BigDecimal calculateLineTotal(CartItem item) {
        BigDecimal unitPrice = getScaledUnitPrice(item.getProduct());
        return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
            .setScale(2, RoundingMode.HALF_UP);
    }
}
