package _2280600764_NguyenTruongGiang.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import _2280600764_NguyenTruongGiang.dto.user.AuthResponse;
import _2280600764_NguyenTruongGiang.dto.user.UserLoginRequest;
import _2280600764_NguyenTruongGiang.dto.user.UserRegisterRequest;
import _2280600764_NguyenTruongGiang.dto.user.UserResponse;
import _2280600764_NguyenTruongGiang.dto.user.UserUpdateRequest;
import _2280600764_NguyenTruongGiang.exception.ResourceNotFoundException;
import _2280600764_NguyenTruongGiang.exception.ValidationException;
import _2280600764_NguyenTruongGiang.model.User;
import _2280600764_NguyenTruongGiang.repository.UserRepository;
import _2280600764_NguyenTruongGiang.security.JwtTokenProvider;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("email", "Email is already registered");
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .phone(request.getPhone())
            .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public AuthResponse authenticate(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ValidationException("email", "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException("password", "Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), JwtTokenProvider.ROLE_USER);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), JwtTokenProvider.ROLE_USER);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
            .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ValidationException("refreshToken", "Invalid or expired refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new ValidationException("refreshToken", "Invalid token type");
        }

        String role = jwtTokenProvider.getRoleFromToken(refreshToken);
        if (!JwtTokenProvider.ROLE_USER.equals(role)) {
            throw new ValidationException("refreshToken", "Invalid token role");
        }

        Long userId = jwtTokenProvider.getIdFromToken(refreshToken);
        String email = jwtTokenProvider.getIdentifierFromToken(refreshToken);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ValidationException("refreshToken", "User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), email, JwtTokenProvider.ROLE_USER);

        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
            .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return mapToResponse(user);
    }

    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getStreet() != null) {
            user.setStreet(request.getStreet());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getPostalCode() != null) {
            user.setPostalCode(request.getPostalCode());
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .street(user.getStreet())
            .city(user.getCity())
            .postalCode(user.getPostalCode())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
