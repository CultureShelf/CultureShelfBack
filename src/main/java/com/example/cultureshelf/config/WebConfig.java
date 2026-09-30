package com.example.cultureshelf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Le back est partage entre plusieurs fronts : chaque origine autorisee est declaree dans
 * {@code cultureshelf.cors.allowed-origins}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CultureShelfProperties properties;

    public WebConfig(CultureShelfProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(properties.cors().allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }
}
