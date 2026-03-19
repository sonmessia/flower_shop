package vn.quahoa.flowershop.controller;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.quahoa.flowershop.config.StorageProperties;
import vn.quahoa.flowershop.security.JwtTokenProvider;

@TestConfiguration
@ComponentScan(basePackages = "vn.quahoa.flowershop",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "vn.quahoa.flowershop.config.WebConfig"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "vn.quahoa.flowershop.config.SecurityConfig"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "vn.quahoa.flowershop.security.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "vn.quahoa.flowershop.config.DataInitializer")
    })
public class TestWebConfig {

    @Bean
    public StorageProperties storageProperties() {
        StorageProperties properties = new StorageProperties();
        properties.setLocalPath("/tmp/test-images");
        properties.setBaseUrl("http://localhost:8081/images");
        return properties;
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return Mockito.mock(JwtTokenProvider.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
