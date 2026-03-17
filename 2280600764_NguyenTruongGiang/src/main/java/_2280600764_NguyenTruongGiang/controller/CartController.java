package _2280600764_NguyenTruongGiang.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import _2280600764_NguyenTruongGiang.dto.cart.CartItemUpsertRequest;
import _2280600764_NguyenTruongGiang.dto.cart.CartResponse;
import _2280600764_NguyenTruongGiang.dto.cart.UpdateCartItemRequest;
import _2280600764_NguyenTruongGiang.model.User;
import _2280600764_NguyenTruongGiang.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal User user) {
        return cartService.getCart(user.getId());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(@AuthenticationPrincipal User user,
                                @Valid @RequestBody CartItemUpsertRequest request) {
        return cartService.addItem(user.getId(), request);
    }

    @PutMapping("/items/{cartItemId}")
    public CartResponse updateItem(@AuthenticationPrincipal User user,
                                   @PathVariable Long cartItemId,
                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(user.getId(), cartItemId, request);
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeItem(@AuthenticationPrincipal User user,
                                   @PathVariable Long cartItemId) {
        return cartService.removeItem(user.getId(), cartItemId);
    }

    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
    }
}
