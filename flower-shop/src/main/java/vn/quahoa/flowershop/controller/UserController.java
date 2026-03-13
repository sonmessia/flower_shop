package vn.quahoa.flowershop.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.quahoa.flowershop.dto.user.AuthResponse;
import vn.quahoa.flowershop.dto.user.RefreshTokenRequest;
import vn.quahoa.flowershop.dto.user.UserLoginRequest;
import vn.quahoa.flowershop.dto.user.UserRegisterRequest;
import vn.quahoa.flowershop.dto.user.UserResponse;
import vn.quahoa.flowershop.dto.user.UserUpdateRequest;
import vn.quahoa.flowershop.model.User;
import vn.quahoa.flowershop.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
    UserResponse user = userService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
    AuthResponse response = userService.authenticate(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    AuthResponse response = userService.refreshToken(request.getRefreshToken());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
    UserResponse response = userService.getById(user.getId());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/me")
  public ResponseEntity<UserResponse> updateProfile(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UserUpdateRequest request) {
    UserResponse response = userService.updateProfile(user.getId(), request);
    return ResponseEntity.ok(response);
  }
}
