package com.example.cultureshelf;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
		"cultureshelf.storage.path=target/test-uploads"
})
class CultureshelfApplicationTests {

	@Test
	void contextLoads() {
	}

}
