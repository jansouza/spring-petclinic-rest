package org.springframework.samples.petclinic;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Was: {@code @SpringBootTest(classes = PetClinicApplication.class)}. {@code @QuarkusTest} boots the whole application.
 */
@QuarkusTest
class SpringConfigTests {

    @Test
    void contextLoads() {
        // Test the application (CDI container) starts
    }
}