package _2280600764_NguyenTruongGiang.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import _2280600764_NguyenTruongGiang.model.Admin;
import _2280600764_NguyenTruongGiang.model.User;
import _2280600764_NguyenTruongGiang.repository.AdminRepository;
import _2280600764_NguyenTruongGiang.repository.UserRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtTokenProvider tokenProvider;
  private final UserRepository userRepository;
  private final AdminRepository adminRepository;

  public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
      UserRepository userRepository,
      AdminRepository adminRepository) {
    this.tokenProvider = tokenProvider;
    this.userRepository = userRepository;
    this.adminRepository = adminRepository;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    try {
      String jwt = getJwtFromRequest(request);
      // logger.debug("JWT from request: {}", jwt != null ? "present" : "absent");

      if (StringUtils.hasText(jwt)) {
        boolean isValid = tokenProvider.validateToken(jwt);
        // logger.debug("Token valid: {}", isValid);

        if (isValid) {
          String tokenType = tokenProvider.getTokenType(jwt);
          // logger.debug("Token type: {}", tokenType);

          if ("access".equals(tokenType)) {
            Long id = tokenProvider.getIdFromToken(jwt);
            String role = tokenProvider.getRoleFromToken(jwt);
            // logger.debug("Token id: {}, role: {}", id, role);

            Object principal = null;
            List<SimpleGrantedAuthority> authorities = Collections.emptyList();

            if (JwtTokenProvider.ROLE_USER.equals(role)) {
              User user = userRepository.findById(id).orElse(null);
              // logger.debug("User found: {}", user != null);
              if (user != null) {
                principal = user;
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
              }
            } else if (JwtTokenProvider.ROLE_ADMIN.equals(role)) {
              Admin admin = adminRepository.findById(id).orElse(null);
              // logger.debug("Admin found: {}", admin != null);
              if (admin != null) {
                principal = admin;
                authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
              }
            }

            if (principal != null) {
              UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,
                  null, authorities);
              authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(authentication);
              // logger.debug("Authentication set with authorities: {}", authorities);
            } else {
              logger.warn("Principal not found for id: {} with role: {}", id, role);
            }
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Could not set user authentication in security context", ex);
    }

    filterChain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
