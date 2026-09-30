package com.example.cultureshelf.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cultureshelf")
public record CultureShelfProperties(Cors cors, Storage storage) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Storage(String path) {
    }
}
