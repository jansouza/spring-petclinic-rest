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
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.api.PetsApi;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.service.ClinicService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitaliy Fedoriv
 */

// Quarkus: was @RestController @CrossOrigin @RequestMapping("/api"). The /api prefix is now @ApplicationPath("/api") on PetClinicApplication and
// CORS is configured with quarkus.http.cors.* in application.properties.
@ApplicationScoped
public class PetRestControllerV1 implements PetsApi {

    private final ClinicService clinicService;

    private final PetMapper petMapper;

    public PetRestControllerV1(ClinicService clinicService, PetMapper petMapper) {
        this.clinicService = clinicService;
        this.petMapper = petMapper;
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response getPet(Integer petId) {
        PetDto pet = petMapper.toPetDto(this.clinicService.findPetById(petId));
        if (pet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(pet).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response listPets() {
        List<PetDto> pets = new ArrayList<>(petMapper.toPetsDto(this.clinicService.findAllPets()));
        if (pets.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(pets).build();
    }


    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response updatePet(Integer petId, PetDto petDto) {
        Pet currentPet = this.clinicService.findPetById(petId);
        if (currentPet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        currentPet.setBirthDate(petDto.getBirthDate());
        currentPet.setName(petDto.getName());
        currentPet.setType(petMapper.toPetType(petDto.getType()));
        this.clinicService.savePet(currentPet);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response deletePet(Integer petId) {
        Pet pet = this.clinicService.findPetById(petId);
        if (pet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.clinicService.deletePet(pet);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
