package vn.quahoa.flowershop.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuthResponse {
  private Long id;
  private String username;
  private String accessToken;
  private String refreshToken;
  @Builder.Default
  private String tokenType = "Bearer";
  private long expiresIn;
}
