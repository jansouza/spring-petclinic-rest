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
import org.springframework.samples.petclinic.mapper.PetTypeMapper;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.api.PettypesApi;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

// Quarkus: was @RestController @CrossOrigin @RequestMapping("/api"). The /api prefix is now @ApplicationPath("/api") on PetClinicApplication and
// CORS is configured with quarkus.http.cors.* in application.properties.
@ApplicationScoped
public class PetTypeRestControllerV1 implements PettypesApi {

    private final ClinicService clinicService;
    private final PetTypeMapper petTypeMapper;


    public PetTypeRestControllerV1(ClinicService clinicService, PetTypeMapper petTypeMapper) {
        this.clinicService = clinicService;
        this.petTypeMapper = petTypeMapper;
    }

    // TODO: Migration required - @PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response listPetTypes() {
        List<PetType> petTypes = new ArrayList<>(this.clinicService.findAllPetTypes());
        if (petTypes.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(petTypeMapper.toPetTypeDtos(petTypes)).build();
    }

    // TODO: Migration required - @PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response getPetType(Integer petTypeId) {
        PetType petType = this.clinicService.findPetTypeById(petTypeId);
        if (petType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(petTypeMapper.toPetTypeDto(petType)).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response addPetType(PetTypeFieldsDto petTypeFieldsDto) {
        final PetType type = petTypeMapper.toPetType(petTypeFieldsDto);
        this.clinicService.savePetType(type);
        String location = "/api/pettypes/" + type.getId();
        return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, location).entity(petTypeMapper.toPetTypeDto(type)).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response updatePetType(Integer petTypeId, PetTypeDto petTypeDto) {
        PetType currentPetType = this.clinicService.findPetTypeById(petTypeId);
        if (currentPetType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        currentPetType.setName(petTypeDto.getName());
        this.clinicService.savePetType(currentPetType);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Transactional
    @Override
    public Response deletePetType(Integer petTypeId) {
        PetType petType = this.clinicService.findPetTypeById(petTypeId);
        if (petType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.clinicService.deletePetType(petType);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
