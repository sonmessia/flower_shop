package vn.quahoa.flowershop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.quahoa.flowershop.security.JwtAuthenticationEntryPoint;
import vn.quahoa.flowershop.security.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Public endpoints - User authentication
            .requestMatchers("/api/users/register", "/api/users/login", "/api/users/refresh").permitAll()
            // Public endpoints - Admin authentication
            .requestMatchers("/api/admins/login", "/api/admins/refresh").permitAll()
            // Products - GET is public, POST/PUT/DELETE require ADMIN
            .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
            // Categories - GET is public, POST/PUT/DELETE require ADMIN
            .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
            // Blogs - public read only (admin endpoints are under /api/admins/blogs/**)
            .requestMatchers(HttpMethod.GET, "/api/blogs/**").permitAll()
            // Static resources
            .requestMatchers("/images/**").permitAll()
            // WebSockets
            .requestMatchers("/ws/**").permitAll()
            // Admin protected endpoints
            .requestMatchers("/api/admins/**").hasRole("ADMIN")
            // User protected endpoints
            .requestMatchers("/api/users/**").hasRole("USER")
            .requestMatchers("/api/cart/**").hasRole("USER")
            .requestMatchers("/api/orders/**").hasRole("USER")
            // Other protected endpoints
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(
        "http://localhost:84",
        "http://127.0.0.1:84",
        "http://localhost:80",
        "http://127.0.0.1:80",
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:8081",
        "http://127.0.0.1:8081"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
