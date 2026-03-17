package _2280600764_NguyenTruongGiang.controller;

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
import _2280600764_NguyenTruongGiang.dto.user.AuthResponse;
import _2280600764_NguyenTruongGiang.dto.user.RefreshTokenRequest;
import _2280600764_NguyenTruongGiang.dto.user.UserLoginRequest;
import _2280600764_NguyenTruongGiang.dto.user.UserRegisterRequest;
import _2280600764_NguyenTruongGiang.dto.user.UserResponse;
import _2280600764_NguyenTruongGiang.dto.user.UserUpdateRequest;
import _2280600764_NguyenTruongGiang.model.User;
import _2280600764_NguyenTruongGiang.service.UserService;

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
