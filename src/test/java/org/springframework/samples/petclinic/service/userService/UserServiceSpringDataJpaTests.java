// TODO: Migration required - Spring Data JPA repository layer (repository/springdatajpa/**) is parked and not compiled.
// This file is kept unchanged but excluded from test compilation (see maven-compiler-plugin testExcludes in pom.xml).
package org.springframework.samples.petclinic.service.userService;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
class UserServiceSpringDataJpaTests extends AbstractUserServiceTests {

}
