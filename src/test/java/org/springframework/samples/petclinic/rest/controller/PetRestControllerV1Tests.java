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

import org.springframework.samples.petclinic.rest.controller.v1.PetRestControllerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.samples.petclinic.service.ClinicService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link PetRestControllerV1}
 *
 * @author Vitaliy Fedoriv
 */

@QuarkusTest
class PetRestControllerV1Tests {

    @InjectMock
    protected ClinicService clinicService;
    @Inject
    ObjectMapper objectMapper;

    @Inject
    PetRestControllerV1 petRestControllerV1;
    @Inject
    PetMapper petMapper;
    private List<PetDto> pets;

    @BeforeEach
    void initPets() {
        pets = new ArrayList<>();

        OwnerDto owner = new OwnerDto();
        owner.id(1).firstName("Eduardo")
            .lastName("Rodriquez")
            .address("2693 Commerce St.")
            .city("McFarland")
            .telephone("6085558763");

        PetTypeDto petType = new PetTypeDto();
        petType.id(2)
            .name("dog");

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
    void testGetPetSuccess() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pets.get(0)));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pets/3")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(3))
                .body("name", equalTo("Rosy"));
    }

    @Test
    void testGetPetNotFound() throws Exception {
        given(this.clinicService.findPetById(999)).willReturn(null);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pets/999")
            .then()
                .statusCode(404);
    }

    @Test
    void testGetAllPetsSuccess() throws Exception {
        final Collection<Pet> mockPets = petMapper.toPets(this.pets);
        when(this.clinicService.findAllPets()).thenReturn(mockPets);

        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pets")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[0].id", equalTo(3))
                .body("[0].name", equalTo("Rosy"))
                .body("[1].id", equalTo(4))
                .body("[1].name", equalTo("Jewel"));
    }

    @Test
    void testGetAllPetsNotFound() throws Exception {
        pets.clear();
        given(this.clinicService.findAllPets()).willReturn(petMapper.toPets(pets));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pets")
            .then()
                .statusCode(404);
    }

    @Test
    void testUpdatePetSuccess() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pets.get(0)));
        PetDto newPet = pets.get(0);
        newPet.setName("Rosy I");
        ObjectMapper mapper = this.objectMapper;

        String newPetAsJSON = mapper.writeValueAsString(newPet);
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .put("/api/pets/3")
            .then()
                .statusCode(204);

        RestAssured.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .get("/api/pets/3")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(3))
                .body("name", equalTo("Rosy I"));

    }

    @Test
    void testUpdatePetError() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setName(null);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);

        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .put("/api/pets/3")
            .then()
                .statusCode(400);
    }

    @Test
    void testDeletePetSuccess() throws Exception {
        PetDto newPet = pets.get(0);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pets.get(0)));
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .delete("/api/pets/3")
            .then()
                .statusCode(204);
    }

    @Test
    void testDeletePetError() throws Exception {
        PetDto newPet = pets.get(0);
        ObjectMapper mapper = this.objectMapper;
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        given(this.clinicService.findPetById(999)).willReturn(null);
        RestAssured.given()
                .body(newPetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .delete("/api/pets/999")
            .then()
                .statusCode(404);
    }

}
