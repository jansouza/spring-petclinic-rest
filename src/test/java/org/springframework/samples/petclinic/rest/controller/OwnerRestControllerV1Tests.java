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

import org.springframework.samples.petclinic.rest.controller.v1.OwnerRestControllerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.service.ClinicService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

/**
 * Test class for {@link OwnerRestControllerV1}
 *
 * @author Vitaliy Fedoriv
 */
@QuarkusTest
class OwnerRestControllerV1Tests {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    OwnerMapper ownerMapper;

    @Inject
    PetMapper petMapper;

    @Inject
    VisitMapper visitMapper;

    @InjectMock
    ClinicService clinicService;

    private List<OwnerDto> owners;

    private List<PetDto> pets;

    private List<VisitDto> visits;

    @BeforeEach
    void initOwners() {
        owners = new ArrayList<>();

        OwnerDto ownerWithPet = new OwnerDto();
        owners.add(ownerWithPet.id(1).firstName("George").lastName("Franklin").address("110 W. Liberty St.").city("Madison").telephone("6085551023").addPetsItem(getTestPetWithIdAndName(1, "Rosy")));
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

        visits = new ArrayList<>();
        VisitDto visit = new VisitDto();
        visit.setId(2);
        visit.setPetId(pet.getId());
        visit.setDate(LocalDate.now());
        visit.setDescription("rabies shot");
        visits.add(visit);

        visit = new VisitDto();
        visit.setId(3);
        visit.setPetId(pet.getId());
        visit.setDate(LocalDate.now());
        visit.setDescription("neutered");
        visits.add(visit);
    }

    private PetDto getTestPetWithIdAndName(final int id, final String name) {
        PetTypeDto petType = new PetTypeDto();
        PetDto pet = new PetDto();
        pet.id(id).name(name).birthDate(LocalDate.now()).type(petType.id(2).name("dog")).addVisitsItem(getTestVisitForPet(1));
        return pet;
    }

    private VisitDto getTestVisitForPet(final int id) {
        VisitDto visit = new VisitDto();
        return visit.id(id).date(LocalDate.now()).description("test" + id);
    }

    @Test
    void testGetOwnerSuccess() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/owners/1")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
                .body("firstName", equalTo("George"));
    }

    @Test
    void testGetOwnerNotFound() throws Exception {
        given(this.clinicService.findOwnerById(2)).willReturn(null);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/owners/2")
            .then()
                .statusCode(404);
    }

    @Test
    void testGetOwnersListSuccess() throws Exception {
        owners.remove(0);
        owners.remove(1);
        given(this.clinicService.findOwnerByLastName("Davis")).willReturn(ownerMapper.toOwners(owners));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/owners?lastName=Davis")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[0].id", equalTo(2))
                .body("[0].firstName", equalTo("Betty"))
                .body("[1].id", equalTo(4))
                .body("[1].firstName", equalTo("Harold"));
    }

    @Test
    void testGetOwnersListNotFound() throws Exception {
        owners.clear();
        given(this.clinicService.findOwnerByLastName("0")).willReturn(ownerMapper.toOwners(owners));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/owners?lastName=0")
            .then()
                .statusCode(404);
    }

    @Test
    void testGetAllOwnersSuccess() throws Exception {
        owners.remove(0);
        owners.remove(1);
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/owners")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[0].id", equalTo(2))
                .body("[0].firstName", equalTo("Betty"))
                .body("[1].id", equalTo(4))
                .body("[1].firstName", equalTo("Harold"));
    }

    @Test
    void testGetAllOwnersNotFound() throws Exception {
        owners.clear();
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/owners")
            .then()
                .statusCode(404);
    }

    @Test
    void testCreateOwnerSuccess() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setId(null);
        ObjectMapper mapper = this.objectMapper;
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        RestAssured.given()
                .body(newOwnerAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners")
            .then()
                .statusCode(201);
    }

    @Test
    void testCreateOwnerError() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setId(null);
        newOwnerDto.setFirstName(null);
        ObjectMapper mapper = this.objectMapper;
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        RestAssured.given()
                .body(newOwnerAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners")
            .then()
                .statusCode(400);
    }

    @Test
    void testUpdateOwnerSuccess() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        int ownerId = owners.get(0).getId();
        OwnerDto updatedOwnerDto = new OwnerDto();
        // body.id = ownerId which is used in url path
        updatedOwnerDto.setId(ownerId);
        updatedOwnerDto.setFirstName("GeorgeI");
        updatedOwnerDto.setLastName("Franklin");
        updatedOwnerDto.setAddress("110 W. Liberty St.");
        updatedOwnerDto.setCity("Madison");
        updatedOwnerDto.setTelephone("6085551023");
        ObjectMapper mapper = this.objectMapper;
        String newOwnerAsJSON = mapper.writeValueAsString(updatedOwnerDto);
        RestAssured.given()
                .body(newOwnerAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .put("/api/owners/" + ownerId)
            .then()
                .statusCode(204);

        RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/owners/" + ownerId)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(ownerId))
                .body("firstName", equalTo("GeorgeI"));

    }

    @Test
    void testUpdateOwnerSuccessNoBodyId() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        int ownerId = owners.get(0).getId();
        OwnerDto updatedOwnerDto = new OwnerDto();
        updatedOwnerDto.setFirstName("GeorgeI");
        updatedOwnerDto.setLastName("Franklin");
        updatedOwnerDto.setAddress("110 W. Liberty St.");
        updatedOwnerDto.setCity("Madison");

        updatedOwnerDto.setTelephone("6085551023");
        ObjectMapper mapper = this.objectMapper;
        String newOwnerAsJSON = mapper.writeValueAsString(updatedOwnerDto);
        RestAssured.given()
                .body(newOwnerAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .put("/api/owners/" + ownerId)
            .then()
                .statusCode(204);

        RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/owners/" + ownerId)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(ownerId))
                .body("firstName", equalTo("GeorgeI"));

    }

    @Test
    void testUpdateOwnerError() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setFirstName("");
        ObjectMapper mapper = this.objectMapper;
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        RestAssured.given()
                .body(newOwnerAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .put("/api/owners/1")
            .then()
                .statusCode(400);
    }

    @Test
    void testDeleteOwnerSuccess() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        ObjectMapper mapper = this.objectMapper;
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        final Owner owner = ownerMapper.toOwner(owners.get(0));
        given(this.clinicService.findOwnerById(1)).willReturn(owner);
        RestAssured.given()
                .body(newOwnerAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .delete("/api/owners/1")
            .then()
                .statusCode(204);
    }

    @Test
    void testDeleteOwnerError() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        ObjectMapper mapper = this.objectMapper;
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        given(this.clinicService.findOwnerById(999)).willReturn(null);
        RestAssured.given()
                .body(newOwnerAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .delete("/api/owners/999")
            .then()
                .statusCode(404);
    }

    @Test
    void testCreatePetSuccess() throws Exception {
        final Owner owner = ownerMapper.toOwner(owners.get(0));
        given(this.clinicService.findOwnerById(1)).willReturn(owner);
        PetDto newPet = pets.get(0);
        newPet.setId(999);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        System.err.println("--> newPetAsJSON=" + newPetAsJSON);
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners/1/pets")
            .then()
                .statusCode(201);
    }

    @Test
    void testCreatePetError() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setId(null);
        newPet.setName(null);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners/1/pets")
            .then()
                .statusCode(400);
    }

    @Test
    void testCreatePetShouldNotExposeTechnicalDetails() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setId(null);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        String technicalMessage = "could not execute statement; SQL [insert into pets ...]; constraint [fk_pet_owner]";
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        doThrow(new PersistenceException(technicalMessage)).when(this.clinicService).savePet(any());
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners/1/pets")
            .then()
                .statusCode(404)
                .body("detail", equalTo("The requested resource could not be processed due to a data constraint violation"));
    }

    @Test
    void testCreatePetWithUnknownOwnerShouldReturnNotFound() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setId(null);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        given(this.clinicService.findOwnerById(1000000)).willReturn(null);
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners/1000000/pets")
            .then()
                .statusCode(404);
    }

    @Test
    void testCreatePetWithNullTypeShouldReturnBadRequestWithGenericDetail() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setId(null);
        newPet.setType(null);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners/1/pets")
            .then()
                .statusCode(400)
                .body("detail", equalTo("The request contains invalid or missing parameters"))
                .body("title", equalTo("ResteasyReactiveViolationException"))
                .body("schemaValidationErrors", instanceOf(java.util.List.class))
                .body("schemaValidationErrors[0].message", notNullValue())
                .body("schemaValidationErrors[0].field", notNullValue())
                .body("schemaValidationErrors[0].rejectedValue", notNullValue())
                .body("schemaValidationErrors[0].defaultMessage", notNullValue());
    }

    @Test
    void testCreatePetWithEmptyTypeNameShouldReturnBadRequestWithGenericDetail() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setId(null);
        newPet.getType().setName("");
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners/1/pets")
            .then()
                .statusCode(400)
                .body("detail", equalTo("The request contains invalid or missing parameters"))
                .body("title", equalTo("ResteasyReactiveViolationException"))
                .body("schemaValidationErrors", instanceOf(java.util.List.class))
                .body("schemaValidationErrors[0].message", notNullValue())
                .body("schemaValidationErrors[0].field", notNullValue())
                .body("schemaValidationErrors[0].rejectedValue", notNullValue())
                .body("schemaValidationErrors[0].defaultMessage", notNullValue());
    }

    @Test
    void testCreatePetWithNullTypeIdShouldReturnBadRequestWithGenericDetail() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setId(null);
        newPet.getType().setId(null);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners/1/pets")
            .then()
                .statusCode(400)
                .body("detail", equalTo("The request contains invalid or missing parameters"))
                .body("title", equalTo("ResteasyReactiveViolationException"))
                .body("schemaValidationErrors", instanceOf(java.util.List.class))
                .body("schemaValidationErrors[0].message", notNullValue())
                .body("schemaValidationErrors[0].field", notNullValue())
                .body("schemaValidationErrors[0].rejectedValue", notNullValue())
                .body("schemaValidationErrors[0].defaultMessage", notNullValue());
    }

    @Test
    void testCreatePetWithUnexpectedErrorShouldReturnInternalServerErrorWithGenericDetail() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setId(null);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        doThrow(new IllegalStateException("JDBC timeout while executing insert into pets"))
            .when(this.clinicService).savePet(any());
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners/1/pets")
            .then()
                .statusCode(500)
                .body("detail", equalTo("An unexpected error occurred while processing your request"))
                .body("title", equalTo("IllegalStateException"))
                .body("timestamp", notNullValue());
    }

    @Test
    void testCreateOwnerWithUnexpectedErrorShouldReturnInternalServerErrorWithGenericDetail() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setId(null);
        ObjectMapper mapper = this.objectMapper;
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        doThrow(new RuntimeException("Low-level persistence exception details"))
            .when(this.clinicService).saveOwner(any());
        RestAssured.given()
                .body(newOwnerAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners")
            .then()
                .statusCode(500)
                .body("detail", equalTo("An unexpected error occurred while processing your request"))
                .body("title", equalTo("RuntimeException"))
                .body("timestamp", notNullValue());
    }

    @Test
    void testCreateVisitSuccess() throws Exception {
        VisitDto newVisit = visits.get(0);
        newVisit.setId(999);
        ObjectMapper mapper = this.objectMapper;
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisit(newVisit));
        System.out.println("newVisitAsJSON " + newVisitAsJSON);
        RestAssured.given()
                .body(newVisitAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/owners/1/pets/1/visits")
            .then()
                .statusCode(201);
    }

    @Test
    void testGetOwnerPetSuccess() throws Exception {
        var owner = ownerMapper.toOwner(owners.get(0));
        given(this.clinicService.findOwnerById(2)).willReturn(owner);
        var pet = petMapper.toPet(pets.get(0));
        pet.setOwner(owner);
        given(this.clinicService.findPetById(1)).willReturn(pet);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/owners/2/pets/1")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testGetOwnersPetsWithOwnerNotFound() throws Exception {
        owners.clear();
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/owners/1/pets/1")
            .then()
                .statusCode(404);
    }

    @Test
    void testGetOwnersPetsWithPetNotFound() throws Exception {
        var owner1 = ownerMapper.toOwner(owners.get(0));
        given(this.clinicService.findOwnerById(1)).willReturn(owner1);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/owners/1/pets/2")
            .then()
                .statusCode(404);
    }

    @Test
    void testUpdateOwnersPetSuccess() throws Exception {
        int ownerId = owners.get(0).getId();
        int petId = pets.get(0).getId();
        given(this.clinicService.findOwnerById(ownerId)).willReturn(ownerMapper.toOwner(owners.get(0)));
        given(this.clinicService.findPetById(petId)).willReturn(petMapper.toPet(pets.get(0)));
        PetDto updatedPetDto = pets.get(0);
        updatedPetDto.setName("Rex");
        updatedPetDto.setBirthDate(LocalDate.of(2020, 1, 15));
        ObjectMapper mapper = this.objectMapper;
        String updatedPetAsJSON = mapper.writeValueAsString(updatedPetDto);
        RestAssured.given()
                .body(updatedPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .put("/api/owners/" + ownerId + "/pets/" + petId)
            .then()
                .statusCode(204);
    }

    @Test
    void testUpdateOwnersPetOwnerNotFound() throws Exception {
        int ownerId = 0;
        int petId = pets.get(0).getId();
        given(this.clinicService.findOwnerById(ownerId)).willReturn(null);
        PetDto petDto = pets.get(0);
        petDto.setName("Thor");
        ObjectMapper mapper = this.objectMapper;
        String updatedPetAsJSON = mapper.writeValueAsString(petDto);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updatedPetAsJSON)
            .when()
                .put("/api/owners/" + ownerId + "/pets/" + petId)
            .then()
                .statusCode(404);
    }

    @Test
    void testUpdateOwnersPetPetNotFound() throws Exception {
        int ownerId = owners.get(0).getId();
        int petId = 0;
        given(this.clinicService.findOwnerById(ownerId)).willReturn(ownerMapper.toOwner(owners.get(0)));
        given(this.clinicService.findPetById(petId)).willReturn(null);
        PetDto petDto = pets.get(0);
        petDto.setName("Ghost");
        petDto.setBirthDate(LocalDate.of(2020, 1, 1));
        ObjectMapper mapper = this.objectMapper;
        String updatedPetAsJSON = mapper.writeValueAsString(petDto);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updatedPetAsJSON)
            .when()
                .put("/api/owners/" + ownerId + "/pets/" + petId)
            .then()
                .statusCode(404);
    }

}
