// TODO: Migration required - Spring @TestConfiguration, only used by the Spring MockMvc tests that were converted to @QuarkusTest; no longer referenced.
// This file is kept unchanged but excluded from test compilation (see maven-compiler-plugin testExcludes in pom.xml).
package org.springframework.samples.petclinic.service.clinicService;

import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class ApplicationTestConfig {

	public ApplicationTestConfig(){
		MockitoAnnotations.openMocks(this);
	}

}
