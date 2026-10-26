/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

/**
 * OpenAPI documentation metadata.
 * <p>
 * Quarkus migration: the Spring {@code @Configuration} with an {@code OpenAPI} bean (springdoc-openapi) is replaced by
 * the MicroProfile OpenAPI {@link OpenAPIDefinition} annotation read by quarkus-smallrye-openapi.
 *
 * @author Vitaliy Fedoriv
 */
@OpenAPIDefinition(
    info = @Info(
        title = "REST Petclinic backend API documentation",
        version = "1.0",
        termsOfService = "https://github.com/spring-petclinic/spring-petclinic-rest/blob/master/terms.txt",
        description = "This is the REST API documentation of the Spring Petclinic backend. "
            + "If authentication is enabled, use admin/admin when calling the APIs",
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"),
        contact = @Contact(
            name = "Vitaliy Fedoriv",
            email = "vitaliy.fedoriv@gmail.com",
            url = "https://github.com/spring-petclinic/spring-petclinic-rest")))
public class SwaggerConfig {
}
