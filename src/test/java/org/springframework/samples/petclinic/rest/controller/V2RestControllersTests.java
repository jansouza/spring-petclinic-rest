package org.springframework.samples.petclinic.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.rest.controller.v2.OwnerRestControllerV2;
import org.springframework.samples.petclinic.rest.controller.v2.PetRestControllerV2;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.samples.petclinic.service.ClinicService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;

@QuarkusTest
public class V2RestControllersTests {
    @Inject
    ObjectMapper objectMapper;

    @Inject
    OwnerMapper ownerMapper;

    @Inject
    PetMapper petMapper;

    @InjectMock
    ClinicService clinicService;

    private List<OwnerDto> owners;

    private List<PetDto> pets;

    @BeforeEach
    void initOwners() {
        owners = new ArrayList<>();

        OwnerDto ownerWithPet = new OwnerDto();
        owners.add(ownerWithPet.id(1).firstName("George").lastName("Franklin").address("110 W. Liberty St.").city("Madison").telephone("6085551023"));
        OwnerDto owner = new OwnerDto();
        owners.add(owner.id(2).firstName("Betty").lastName("Davis").address("638 Cardinal Ave.").city("Sun Prairie").telephone("6085551749"));
        owner = new OwnerDto();
        owners.add(owner.id(3).firstName("Eduardo").lastName("Rodriquez").address("2693 Commerce St.").city("McFarland").telephone("6085558763"));
        owner = new OwnerDto();
        owners.add(owner.id(4).firstName("Harold").lastName("Davis").address("563 Friendly St.").city("Windsor").telephone("6085553198"));

        PetTypeDto petType = new PetTypeDto();
        petType.id(2)
            .name("dog");

        pets = new ArrayList<>();
        PetDto pet = new PetDto();
        pets.add(pet.id(3)
            .name("Rosy")
            .birthDate(LocalDate.now())
            .type(petType));

        pet = new PetDto();
        pets.add(pet.id(4)
            .name("Jewel")
            .birthDate(LocalDate.now())
            .type(petType));
    }

    @Test
    void testGetOwnersPageSuccess() throws Exception {
        var pageRequest = PageRequest.of(0, 2, Sort.by("id"));
        var pageOwners = ownerMapper.toOwners(owners.subList(0, 2)).stream().toList();
        given(this.clinicService.findOwners(null, pageRequest))
            .willReturn(new PageImpl<>(pageOwners, pageRequest, owners.size()));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/v2/owners?page=0&size=2")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("content[0].id", equalTo(1))
                .body("content[0].firstName", equalTo("George"))
                .body("content[1].id", equalTo(2))
                .body("content[1].firstName", equalTo("Betty"))
                .body("page", equalTo(0))
                .body("size", equalTo(2))
                .body("totalElements", equalTo(4))
                .body("totalPages", equalTo(2));
    }

    @Test
    void testGetPetsPageSuccess() throws Exception {
        var pageRequest = PageRequest.of(0, 5, Sort.by("id"));
        var pagePets = petMapper.toPets(pets).stream().toList();
        given(this.clinicService.findPets(pageRequest))
            .willReturn(new PageImpl<>(pagePets, pageRequest, pets.size()));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/v2/pets?page=0&size=5")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("content[0].id", equalTo(3))
                .body("content[0].name", equalTo("Rosy"))
                .body("page", equalTo(0))
                .body("size", equalTo(5))
                .body("totalElements", equalTo(2))
                .body("totalPages", equalTo(1));
    }
}
