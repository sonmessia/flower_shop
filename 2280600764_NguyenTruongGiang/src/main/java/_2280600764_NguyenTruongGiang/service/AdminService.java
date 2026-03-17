package _2280600764_NguyenTruongGiang.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import _2280600764_NguyenTruongGiang.dto.admin.AdminAuthResponse;
import _2280600764_NguyenTruongGiang.dto.admin.AdminCreateRequest;
import _2280600764_NguyenTruongGiang.dto.admin.AdminLoginRequest;
import _2280600764_NguyenTruongGiang.dto.admin.AdminUpdateRequest;
import _2280600764_NguyenTruongGiang.exception.ResourceNotFoundException;
import _2280600764_NguyenTruongGiang.exception.ValidationException;
import _2280600764_NguyenTruongGiang.model.Admin;
import _2280600764_NguyenTruongGiang.repository.AdminRepository;
import _2280600764_NguyenTruongGiang.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public Admin createAdmin(AdminCreateRequest request) {
    validateUsernameUnique(request.getUsername(), null);
    Admin admin = new Admin();
    admin.setUsername(request.getUsername());
    admin.setPassword(passwordEncoder.encode(request.getPassword()));
    return adminRepository.save(admin);
  }

  public List<Admin> findAll() {
    return adminRepository.findAll();
  }

  public Admin getById(Long id) {
    return adminRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Admin", id));
  }

  public Admin updateAdmin(Long id, AdminUpdateRequest request) {
    Admin admin = getById(id);
    validateUsernameUnique(request.getUsername(), id);
    admin.setUsername(request.getUsername());
    if (StringUtils.hasText(request.getPassword())) {
      admin.setPassword(passwordEncoder.encode(request.getPassword()));
    }
    return adminRepository.save(admin);
  }

  public void deleteAdmin(Long id) {
    Admin admin = getById(id);
    adminRepository.delete(admin);
  }

  public AdminAuthResponse authenticate(AdminLoginRequest request) {
    Admin admin = adminRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new ValidationException("credentials", "Invalid username or password"));

    if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
      throw new ValidationException("credentials", "Invalid username or password");
    }

    String accessToken = jwtTokenProvider.generateAccessToken(admin.getId(), admin.getUsername(),
        JwtTokenProvider.ROLE_ADMIN);
    String refreshToken = jwtTokenProvider.generateRefreshToken(admin.getId(), admin.getUsername(),
        JwtTokenProvider.ROLE_ADMIN);

    return AdminAuthResponse.builder()
        .id(admin.getId())
        .username(admin.getUsername())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
        .build();
  }

  public AdminAuthResponse refreshToken(String refreshToken) {
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new ValidationException("refreshToken", "Invalid or expired refresh token");
    }

    String tokenType = jwtTokenProvider.getTokenType(refreshToken);
    if (!"refresh".equals(tokenType)) {
      throw new ValidationException("refreshToken", "Invalid token type");
    }

    String role = jwtTokenProvider.getRoleFromToken(refreshToken);
    if (!JwtTokenProvider.ROLE_ADMIN.equals(role)) {
      throw new ValidationException("refreshToken", "Invalid token role");
    }

    Long adminId = jwtTokenProvider.getIdFromToken(refreshToken);
    String username = jwtTokenProvider.getIdentifierFromToken(refreshToken);

    Admin admin = adminRepository.findById(adminId)
        .orElseThrow(() -> new ValidationException("refreshToken", "Admin not found"));

    String newAccessToken = jwtTokenProvider.generateAccessToken(admin.getId(), username, JwtTokenProvider.ROLE_ADMIN);

    return AdminAuthResponse.builder()
        .id(admin.getId())
        .username(admin.getUsername())
        .accessToken(newAccessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
        .build();
  }

  private void validateUsernameUnique(String username, Long currentId) {
    adminRepository.findByUsername(username).ifPresent(existing -> {
      if (currentId == null || !existing.getId().equals(currentId)) {
        throw new ValidationException("username", "Username already exists");
      }
    });
  }
}
