package org.springframework.samples.petclinic.service.clinicService;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * <p> Integration test using the jpa profile.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @see AbstractClinicServiceTests AbstractClinicServiceTests for more details. </p>
 */

@QuarkusTest
class ClinicServiceJpaTests extends AbstractClinicServiceTests {

    @Inject
    EntityManager entityManager;

    @Override
    void clearCache() {
        entityManager.clear();
    }
}
