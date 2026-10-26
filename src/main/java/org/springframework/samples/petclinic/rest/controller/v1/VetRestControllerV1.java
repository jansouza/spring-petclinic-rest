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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.rest.api.VetsApi;
import org.springframework.samples.petclinic.rest.dto.VetDto;
import org.springframework.samples.petclinic.service.ClinicService;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vitaliy Fedoriv
 */

// Quarkus: was @RestController @CrossOrigin @RequestMapping("/api"). The /api prefix is now @ApplicationPath("/api") on PetClinicApplication and
// CORS is configured with quarkus.http.cors.* in application.properties.
@ApplicationScoped
public class VetRestControllerV1 implements VetsApi {

    private final ClinicService clinicService;
    private final VetMapper vetMapper;
    private final SpecialtyMapper specialtyMapper;

    public VetRestControllerV1(ClinicService clinicService, VetMapper vetMapper, SpecialtyMapper specialtyMapper) {
        this.clinicService = clinicService;
        this.vetMapper = vetMapper;
        this.specialtyMapper = specialtyMapper;
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response listVets() {
        List<VetDto> vets = new ArrayList<>(vetMapper.toVetDtos(this.clinicService.findAllVets()));
        if (vets.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(vets).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response getVet(Integer vetId)  {
        Vet vet = this.clinicService.findVetById(vetId);
        if (vet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(vetMapper.toVetDto(vet)).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response addVet(VetDto vetDto) {
        Vet vet = vetMapper.toVet(vetDto);
        if(vet.getNrOfSpecialties() > 0){
            List<Specialty> vetSpecialities = this.clinicService.findSpecialtiesByNameIn(vet.getSpecialties().stream().map(Specialty::getName).collect(Collectors.toSet()));
            vet.setSpecialties(vetSpecialities);
        }
        this.clinicService.saveVet(vet);
        String location = "/api/vets/" + vet.getId();
        return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, location).entity(vetMapper.toVetDto(vet)).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Override
    public Response updateVet(Integer vetId,VetDto vetDto)  {
        Vet currentVet = this.clinicService.findVetById(vetId);
        if (currentVet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        currentVet.setFirstName(vetDto.getFirstName());
        currentVet.setLastName(vetDto.getLastName());
        currentVet.clearSpecialties();
        for (Specialty spec : specialtyMapper.toSpecialtys(vetDto.getSpecialties())) {
            currentVet.addSpecialty(spec);
        }
        if(currentVet.getNrOfSpecialties() > 0){
            List<Specialty> vetSpecialities = this.clinicService.findSpecialtiesByNameIn(currentVet.getSpecialties().stream().map(Specialty::getName).collect(Collectors.toSet()));
            currentVet.setSpecialties(vetSpecialities);
        }
        this.clinicService.saveVet(currentVet);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    // TODO: Migration required - @PreAuthorize("hasRole(@roles.VET_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    @Transactional
    @Override
    public Response deleteVet(Integer vetId) {
        Vet vet = this.clinicService.findVetById(vetId);
        if (vet == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.clinicService.deleteVet(vet);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
