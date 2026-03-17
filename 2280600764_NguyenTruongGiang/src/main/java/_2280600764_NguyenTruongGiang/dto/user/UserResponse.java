package _2280600764_NguyenTruongGiang.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String street;
    private String city;
    private String postalCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
