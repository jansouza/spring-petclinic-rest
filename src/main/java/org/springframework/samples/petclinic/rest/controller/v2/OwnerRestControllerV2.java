package org.springframework.samples.petclinic.rest.controller.v2;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.api.OwnerV2Api;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.ClinicService;

// Quarkus: was @RestController @CrossOrigin @RequestMapping("/api"). The /api prefix is now @ApplicationPath("/api") on PetClinicApplication and
// CORS is configured with quarkus.http.cors.* in application.properties.
@ApplicationScoped
public class OwnerRestControllerV2 implements OwnerV2Api {

    private final ClinicService clinicService;
    private final OwnerMapper ownerMapper;

    public OwnerRestControllerV2(ClinicService clinicService, OwnerMapper ownerMapper) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
    }

    @Override
    // TODO: Migration required - @PreAuthorize("hasRole(@roles.OWNER_ADMIN)") not applied: Spring Security SpEL (@roles bean) is not supported by quarkus-spring-security.
    public Response listOwnersPage(String lastName, Integer page, Integer size) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 20 : size;
        Page<Owner> owners = this.clinicService.findOwners(
            lastName,
            PageRequest.of(pageNumber, pageSize, Sort.by("id")));
        return Response.ok(ownerMapper.toOwnerPageDto(owners)).build();
    }
}
