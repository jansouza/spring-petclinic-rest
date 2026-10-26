// TODO: Migration required - Spring JDBC repository layer (profile 'jdbc', repository/jdbc/**) is parked and not compiled.
// This file is kept unchanged but excluded from test compilation (see maven-compiler-plugin testExcludes in pom.xml).
package org.springframework.samples.petclinic.service.userService;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles({"h2", "jdbc"})
@TestPropertySource(properties = {
    "spring.sql.init.platform=h2",
    "spring.h2.console.enabled=false"
})
class UserServiceH2JdbcTests extends AbstractUserServiceTests {

}
