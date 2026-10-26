/*
 * Copyright 2016-2017 the original author or authors.
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.api.VisitsApi;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitaliy Fedoriv
 */

// Quarkus: was @RestController @CrossOrigin @RequestMapping("/api"). The /api prefix is now @ApplicationPath("/api") on PetClinicApplication and
// CORS is configured with quarkus.http.cors.* in application.properties.
@ApplicationScoped
public class VisitRestControllerV1 implements VisitsApi {

    private final ClinicService clinicService;

    private final VisitMapper visitMapper;

    public VisitRestControllerV1(ClinicService clinicService, VisitMapper visitMapper) {
        this.clinicService = clinicService;
        this.visitMapper = visitMapper;
    }


    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response listVisits() {
        List<Visit> visits = new ArrayList<>(this.clinicService.findAllVisits());
        if (visits.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new ArrayList<>(visitMapper.toVisitsDto(visits))).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response getVisit( Integer visitId) {
        Visit visit = this.clinicService.findVisitById(visitId);
        if (visit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(visitMapper.toVisitDto(visit)).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response addVisit(VisitDto visitDto) {
        Visit visit = visitMapper.toVisit(visitDto);
        this.clinicService.saveVisit(visit);
        visitDto = visitMapper.toVisitDto(visit);
        String location = "/api/visits/" + visit.getId();
        return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, location).entity(visitDto).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response updateVisit(Integer visitId, VisitFieldsDto visitDto) {
        Visit currentVisit = this.clinicService.findVisitById(visitId);
        if (currentVisit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        currentVisit.setDate(visitDto.getDate());
        currentVisit.setDescription(visitDto.getDescription());
        this.clinicService.saveVisit(currentVisit);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Transactional
    @Override
    public Response deleteVisit(Integer visitId) {
        Visit visit = this.clinicService.findVisitById(visitId);
        if (visit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.clinicService.deleteVisit(visit);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
