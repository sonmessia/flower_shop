package vn.quahoa.flowershop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.quahoa.flowershop.dto.user.AuthResponse;
import vn.quahoa.flowershop.dto.user.UserLoginRequest;
import vn.quahoa.flowershop.dto.user.UserRegisterRequest;
import vn.quahoa.flowershop.dto.user.UserResponse;
import vn.quahoa.flowershop.dto.user.UserUpdateRequest;
import vn.quahoa.flowershop.exception.ResourceNotFoundException;
import vn.quahoa.flowershop.exception.ValidationException;
import vn.quahoa.flowershop.model.User;
import vn.quahoa.flowershop.repository.UserRepository;
import vn.quahoa.flowershop.security.JwtTokenProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegisterRequest registerRequest;
    private UserLoginRequest loginRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("encodedPassword")
            .fullName("Test User")
            .phone("1234567890")
            .build();

        registerRequest = UserRegisterRequest.builder()
            .email("newuser@example.com")
            .password("password123")
            .fullName("New User")
            .phone("0987654321")
            .build();

        loginRequest = UserLoginRequest.builder()
            .email("test@example.com")
            .password("password123")
            .build();

        updateRequest = UserUpdateRequest.builder()
            .fullName("Updated User")
            .phone("9999999999")
            .street("123 Main St")
            .city("New York")
            .postalCode("10001")
            .build();
    }

    @Test
    void register_ValidUser_ReturnsUserResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFullName(), response.getFullName());
        assertEquals(testUser.getPhone(), response.getPhone());
        
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsValidationException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.register(registerRequest)
        );

        assertEquals("email", exception.getField());
        assertEquals("Email is already registered", exception.getMessage());
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_ValidCredentials_ReturnsAuthResponse() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyLong(), anyString(), anyString())).thenReturn("refreshToken");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

        AuthResponse response = userService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    void authenticate_InvalidEmail_ThrowsValidationException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.authenticate(loginRequest)
        );

        assertEquals("email", exception.getField());
        assertEquals("Invalid email or password", exception.getMessage());
        
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_InvalidPassword_ThrowsValidationException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.authenticate(loginRequest)
        );

        assertEquals("password", exception.getField());
        assertEquals("Invalid email or password", exception.getMessage());
        
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    void refreshToken_ValidRefreshToken_ReturnsNewAccessToken() {
        String refreshToken = "validRefreshToken";
        
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getTokenType(anyString())).thenReturn("refresh");
        when(jwtTokenProvider.getRoleFromToken(anyString())).thenReturn("USER");
        when(jwtTokenProvider.getIdFromToken(anyString())).thenReturn(1L);
        when(jwtTokenProvider.getIdentifierFromToken(anyString())).thenReturn("test@example.com");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString())).thenReturn("newAccessToken");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

        AuthResponse response = userService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
    }

    @Test
    void refreshToken_InvalidToken_ThrowsValidationException() {
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(false);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.refreshToken("invalidToken")
        );

        assertEquals("refreshToken", exception.getField());
        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }

    @Test
    void refreshToken_WrongTokenType_ThrowsValidationException() {
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getTokenType(anyString())).thenReturn("access");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.refreshToken("accessToken")
        );

        assertEquals("refreshToken", exception.getField());
        assertEquals("Invalid token type", exception.getMessage());
    }

    @Test
    void refreshToken_UserNotFound_ThrowsValidationException() {
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getTokenType(anyString())).thenReturn("refresh");
        when(jwtTokenProvider.getRoleFromToken(anyString())).thenReturn("USER");
        when(jwtTokenProvider.getIdFromToken(anyString())).thenReturn(999L);
        when(jwtTokenProvider.getIdentifierFromToken(anyString())).thenReturn("nonexistent@example.com");
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.refreshToken("validRefreshToken")
        );

        assertEquals("refreshToken", exception.getField());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getById_ExistingUser_ReturnsUserResponse() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getById(1L);

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFullName(), response.getFullName());
        
        verify(userRepository).findById(1L);
    }

    @Test
    void getById_NonExistingUser_ThrowsResourceNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getById(999L)
        );

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void getByEmail_ExistingUser_ReturnsUserResponse() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getByEmail("test@example.com");

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFullName(), response.getFullName());
        
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getByEmail_NonExistingUser_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getByEmail("nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
    }

    @Test
    void updateProfile_ValidUpdate_ReturnsUpdatedUserResponse() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateProfile(1L, updateRequest);

        assertNotNull(response);
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        
        assertEquals(updateRequest.getFullName(), testUser.getFullName());
        assertEquals(updateRequest.getPhone(), testUser.getPhone());
        assertEquals(updateRequest.getStreet(), testUser.getStreet());
        assertEquals(updateRequest.getCity(), testUser.getCity());
        assertEquals(updateRequest.getPostalCode(), testUser.getPostalCode());
    }

    @Test
    void updateProfile_NonExistingUser_ThrowsResourceNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.updateProfile(999L, updateRequest)
        );

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("999"));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_PartialUpdate_UpdatesOnlyProvidedFields() {
        UserUpdateRequest partialUpdate = UserUpdateRequest.builder()
            .fullName("Only Name Updated")
            .build();
            
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateProfile(1L, partialUpdate);

        assertNotNull(response);
        assertEquals("Only Name Updated", testUser.getFullName());
        assertNull(testUser.getCity());
        assertNull(testUser.getStreet());
        
        verify(userRepository).save(testUser);
    }
}
