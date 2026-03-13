package vn.quahoa.flowershop.controller;

import java.util.List;

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
import vn.quahoa.flowershop.dto.admin.AdminAuthResponse;
import vn.quahoa.flowershop.dto.admin.AdminCreateRequest;
import vn.quahoa.flowershop.dto.admin.AdminLoginRequest;
import vn.quahoa.flowershop.dto.admin.AdminUpdateRequest;
import vn.quahoa.flowershop.dto.user.RefreshTokenRequest;
import vn.quahoa.flowershop.model.Admin;
import vn.quahoa.flowershop.service.AdminService;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Admin createAdmin(@Valid @RequestBody AdminCreateRequest request) {
    return adminService.createAdmin(request);
  }

  @PostMapping("/login")
  public AdminAuthResponse login(@Valid @RequestBody AdminLoginRequest request) {
    return adminService.authenticate(request);
  }

  @PostMapping("/refresh")
  public AdminAuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return adminService.refreshToken(request.getRefreshToken());
  }

  @GetMapping("/me")
  public Admin getCurrentAdmin(@AuthenticationPrincipal Admin admin) {
    return adminService.getById(admin.getId());
  }

  @GetMapping
  public List<Admin> getAllAdmins() {
    return adminService.findAll();
  }

  @GetMapping("/{id:\\d+}")
  public Admin getAdmin(@PathVariable Long id) {
    return adminService.getById(id);
  }

  @PutMapping("/{id:\\d+}")
  public Admin updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminUpdateRequest request) {
    return adminService.updateAdmin(id, request);
  }

  @DeleteMapping("/{id:\\d+}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAdmin(@PathVariable Long id) {
    adminService.deleteAdmin(id);
  }
}
