package org.springframework.samples.petclinic;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

// REMOVED: @SpringBootApplication, SpringBootServletInitializer and SpringApplication.run(...) - Quarkus has its own
// bootstrap (no main class / WAR initializer needed). The Spring Boot application class is replaced by a JAX-RS
// Application that provides the "/api" prefix previously declared on every controller with @RequestMapping("/api").
@ApplicationPath("/api")
public class PetClinicApplication extends Application {
}
