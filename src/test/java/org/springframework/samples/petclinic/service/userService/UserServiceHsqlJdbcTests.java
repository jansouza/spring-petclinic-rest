// TODO: Migration required - Spring JDBC repository layer (profile 'jdbc', repository/jdbc/**) is parked and not compiled; HSQLDB is also unsupported in Quarkus.
// This file is kept unchanged but excluded from test compilation (see maven-compiler-plugin testExcludes in pom.xml).
package org.springframework.samples.petclinic.service.userService;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"hsqldb", "jdbc"})
class UserServiceHsqlJdbcTests extends AbstractUserServiceTests {

}
