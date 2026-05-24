package com.tutor.gurukul;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceApplicationTests {

    @LocalServerPort
    Integer port;

	@Test
	void contextLoads() {
        System.out.println("Running tests on port: " + port);
	}

}
