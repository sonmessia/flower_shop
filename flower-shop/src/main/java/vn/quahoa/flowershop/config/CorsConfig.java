package vn.quahoa.flowershop.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private static final List<String> ALLOWED_ORIGINS = List.of(
      "http://localhost:84",
      "http://127.0.0.1:84",
      "http://localhost:80",
      "http://127.0.0.1:80",
      "http://localhost:5173",
      "http://127.0.0.1:5173",
      "http://localhost:8081",
      "http://127.0.0.1:8081"
  );

  /**
   * CorsFilter runs at the servlet filter level (before DispatcherServlet),
   * ensuring OPTIONS preflight requests always receive the correct headers.
   */
  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(ALLOWED_ORIGINS);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    source.registerCorsConfiguration("/images/**", config);

    return new CorsFilter(source);
  }

  /** Keep MVC-level CORS in sync for completeness. */
  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins(ALLOWED_ORIGINS.toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }
}
