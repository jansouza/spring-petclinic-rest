/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.advice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.springframework.samples.petclinic.rest.controller.BindingErrorsResponse;
import org.springframework.samples.petclinic.rest.dto.ProblemDetailDto;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;

/**
 * Global Exception handler for REST controllers.
 * <p>
 * This class handles exceptions thrown by REST controllers and returns appropriate HTTP responses to the client.
 * <p>
 * Quarkus migration: this class used to be a Spring {@code @ControllerAdvice} with {@code @ExceptionHandler} methods
 * returning {@code ResponseEntity<ProblemDetail>}. The handlers are now Quarkus REST {@link ServerExceptionMapper}s
 * returning the generated {@link ProblemDetailDto} (same fields as the Spring {@code ProblemDetail}). The mapping is:
 * <ul>
 * <li>{@code Exception} -&gt; 500 (unchanged)</li>
 * <li>{@code DataIntegrityViolationException} (Spring DAO) -&gt; {@link PersistenceException} (JPA) -&gt; 404 (unchanged status)</li>
 * <li>{@code MethodArgumentNotValidException} (Spring MVC) -&gt; {@link ConstraintViolationException} (Bean Validation) -&gt; 400</li>
 * </ul>
 *
 * @author Vitaliy Fedoriv
 * @author Alexander Dudkin
 */
public class ExceptionControllerAdvice {

    private static final Logger logger = Logger.getLogger(ExceptionControllerAdvice.class);
    private static final String ERROR_UNEXPECTED = "An unexpected error occurred while processing your request";
    private static final String ERROR_DATA_INTEGRITY = "The requested resource could not be processed due to a data constraint violation";
    private static final String ERROR_INVALID_REQUEST = "The request contains invalid or missing parameters";
    private static final String PROBLEM_JSON = "application/problem+json";

    /**
     * Private method for constructing the {@link ProblemDetailDto} object passing the name and details of the exception
     * class.
     *
     * @param e       Object referring to the thrown exception.
     * @param status  HTTP response status.
     * @param uriInfo Information about the current request URI.
     */
    private ProblemDetailDto detailBuild(Exception e, Response.Status status, UriInfo uriInfo, String detail) {
        return new ProblemDetailDto()
            .type(uriInfo.getRequestUri())
            .title(e.getClass().getSimpleName())
            .status(status.getStatusCode())
            .detail(detail)
            .timestamp(OffsetDateTime.now())
            .schemaValidationErrors(List.<ValidationMessageDto>of());
    }

    private Response build(Response.Status status, ProblemDetailDto detail) {
        return Response.status(status).type(PROBLEM_JSON).entity(detail).build();
    }

    /**
     * Handles all general exceptions by returning a 500 Internal Server Error status with error details.
     * <p>
     * JAX-RS {@link WebApplicationException}s (for example 404 for an unknown path or 405) keep their own response.
     */
    @ServerExceptionMapper
    public Response handleGeneralException(Exception e, UriInfo uriInfo) {
        if (e instanceof WebApplicationException webApplicationException) {
            return webApplicationException.getResponse();
        }
        logger.errorf(e, "Unexpected error at %s", uriInfo.getRequestUri());
        return build(Response.Status.INTERNAL_SERVER_ERROR,
            this.detailBuild(e, Response.Status.INTERNAL_SERVER_ERROR, uriInfo, ERROR_UNEXPECTED));
    }

    /**
     * Handles {@link PersistenceException} which typically indicates database constraint violations. This
     * method returns a 404 Not Found status if an entity does not exist.
     * <p>
     * TODO: Migration required - Spring's DataIntegrityViolationException was raised by Spring's exception translation,
     * which does not exist in Quarkus; every JPA PersistenceException is mapped here to keep the 404 behaviour.
     */
    @ServerExceptionMapper
    public Response handlePersistenceException(PersistenceException e, UriInfo uriInfo) {
        logger.warnf("Data integrity violation at %s: %s", uriInfo.getRequestUri(), e.getMessage());
        logger.debug("Data integrity violation stacktrace", e);
        return build(Response.Status.NOT_FOUND,
            this.detailBuild(e, Response.Status.NOT_FOUND, uriInfo, ERROR_DATA_INTEGRITY));
    }

    /**
     * Handles exception thrown by Bean Validation on resource method parameters.
     *
     * @return A {@link Response} containing the error information and a 400 Bad Request status.
     */
    @ServerExceptionMapper
    public Response handleConstraintViolationException(ConstraintViolationException e, UriInfo uriInfo) {
        Response.Status status = Response.Status.BAD_REQUEST;
        BindingErrorsResponse errors = new BindingErrorsResponse();
        ProblemDetailDto detail = this.detailBuild(e, status, uriInfo, ERROR_INVALID_REQUEST);
        if (!e.getConstraintViolations().isEmpty()) {
            errors.addAllErrors(e.getConstraintViolations());
            List<ValidationMessageDto> schemaValidationErrors = e.getConstraintViolations().stream()
                .map(this::toValidationMessage)
                .toList();
            logger.debugf("Validation error at %s: %s", uriInfo.getRequestUri(), e.getConstraintViolations());
            detail.setSchemaValidationErrors(schemaValidationErrors);
        }
        return build(status, detail);
    }

    private ValidationMessageDto toValidationMessage(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        String rejectedValue = Objects.toString(violation.getInvalidValue(), "null");
        String defaultMessage = Objects.toString(violation.getMessage(), "Validation failed");
        String message = "Field '%s' %s (rejected value: %s)".formatted(field, defaultMessage, rejectedValue);
        return new ValidationMessageDto().message(message)
            .putAdditionalProperty("field", field)
            .putAdditionalProperty("rejectedValue", rejectedValue)
            .putAdditionalProperty("defaultMessage", defaultMessage);
    }

}
