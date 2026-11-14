package elin.le.giftly;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Flyway migrations are PostgreSQL-specific and incompatible with H2 test database")
class GiftlyApplicationTests {

	@Test
	void contextLoads() {
	}

}
