/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller.v1;

import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Redirects the application root to the Swagger UI.
 * <p>
 * Quarkus migration: was a Spring {@code @RestController} mapped to "/" that used {@code HttpServletResponse.sendRedirect}
 * and {@code @Value("#{servletContext.contextPath}")}. The servlet API is not available in Quarkus, and a JAX-RS
 * resource would now live under the "/api" application path, so the redirect is registered as a Vert.x route instead.
 * The {@code @CrossOrigin} setting is replaced by {@code quarkus.http.cors.*} in application.properties.
 *
 * @author Vitaliy Fedoriv
 */
@ApplicationScoped
public class RootRestControllerV1 {

    @ConfigProperty(name = "quarkus.http.root-path", defaultValue = "/")
    String rootPath;

    void redirectToSwagger(@Observes Router router) {
        String contextPath = this.rootPath.endsWith("/") ? this.rootPath : this.rootPath + "/";
        router.get(contextPath).handler(ctx -> ctx.redirect(contextPath + "q/swagger-ui/"));
    }

}
