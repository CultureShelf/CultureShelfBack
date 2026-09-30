package com.example.cultureshelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CultureshelfApplication {

	public static void main(String[] args) {
        SpringApplication.run(CultureshelfApplication.class, args);
	}

}
