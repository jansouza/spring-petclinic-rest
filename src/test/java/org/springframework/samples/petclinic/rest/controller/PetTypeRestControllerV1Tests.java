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

import org.springframework.samples.petclinic.rest.controller.v1.PetTypeRestControllerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.PetTypeMapper;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;

/**
 * Test class for {@link PetTypeRestControllerV1}
 *
 * @author Vitaliy Fedoriv
 */
@QuarkusTest
class PetTypeRestControllerV1Tests {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    PetTypeMapper petTypeMapper;

    @InjectMock
    ClinicService clinicService;

    private List<PetType> petTypes;

    @BeforeEach
    void initPetTypes(){
    	petTypes = new ArrayList<>();

    	PetType petType = new PetType();
    	petType.setId(1);
    	petType.setName("cat");
    	petTypes.add(petType);

    	petType = new PetType();
    	petType.setId(2);
    	petType.setName("dog");
    	petTypes.add(petType);

    	petType = new PetType();
    	petType.setId(3);
    	petType.setName("lizard");
    	petTypes.add(petType);

    	petType = new PetType();
    	petType.setId(4);
    	petType.setName("snake");
    	petTypes.add(petType);
    }

    @Test
    void testGetPetTypeSuccessAsOwnerAdmin() throws Exception {
    	given(this.clinicService.findPetTypeById(1)).willReturn(petTypes.get(0));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pettypes/1")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
                .body("name", equalTo("cat"));
    }

    @Test
    void testGetPetTypeSuccessAsVetAdmin() throws Exception {
        given(this.clinicService.findPetTypeById(1)).willReturn(petTypes.get(0));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pettypes/1")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
                .body("name", equalTo("cat"));
    }

    @Test
    void testGetPetTypeNotFound() throws Exception {
    	given(this.clinicService.findPetTypeById(999)).willReturn(null);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pettypes/999")
            .then()
                .statusCode(404);
    }

    @Test
    void testGetAllPetTypesSuccessAsOwnerAdmin() throws Exception {
    	petTypes.remove(0);
    	petTypes.remove(1);
    	given(this.clinicService.findAllPetTypes()).willReturn(petTypes);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pettypes")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[0].id", equalTo(2))
                .body("[0].name", equalTo("dog"))
                .body("[1].id", equalTo(4))
                .body("[1].name", equalTo("snake"));
    }

    @Test
    void testGetAllPetTypesSuccessAsVetAdmin() throws Exception {
        petTypes.remove(0);
        petTypes.remove(1);
        given(this.clinicService.findAllPetTypes()).willReturn(petTypes);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pettypes")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[0].id", equalTo(2))
                .body("[0].name", equalTo("dog"))
                .body("[1].id", equalTo(4))
                .body("[1].name", equalTo("snake"));
    }

    @Test
    void testGetAllPetTypesNotFound() throws Exception {
    	petTypes.clear();
    	given(this.clinicService.findAllPetTypes()).willReturn(petTypes);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/pettypes")
            .then()
                .statusCode(404);
    }

    @Test
    void testCreatePetTypeSuccess() throws Exception {
    	PetType newPetType = petTypes.get(0);
    	newPetType.setId(null);
    	ObjectMapper mapper = this.objectMapper;
        String newPetTypeAsJSON = mapper.writeValueAsString(petTypeMapper.toPetTypeFieldsDto(newPetType));
    	RestAssured.given()
    	        .body(newPetTypeAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .post("/api/pettypes")
    	    .then()
    	        .statusCode(201);
    }

    @Test
    void testCreatePetTypeError() throws Exception {
    	PetType newPetType = petTypes.get(0);
    	newPetType.setId(null);
    	newPetType.setName(null);
    	ObjectMapper mapper = this.objectMapper;
        String newPetTypeAsJSON = mapper.writeValueAsString(petTypeMapper.toPetTypeDto(newPetType));
    	RestAssured.given()
    	        .body(newPetTypeAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .post("/api/pettypes")
    	    .then()
    	        .statusCode(400);
     }

    @Test
    void testUpdatePetTypeSuccess() throws Exception {
    	given(this.clinicService.findPetTypeById(2)).willReturn(petTypes.get(1));
    	PetType newPetType = petTypes.get(1);
    	newPetType.setName("dog I");
    	ObjectMapper mapper = this.objectMapper;
        String newPetTypeAsJSON = mapper.writeValueAsString(petTypeMapper.toPetTypeDto(newPetType));
    	RestAssured.given()
    	        .body(newPetTypeAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .put("/api/pettypes/2")
    	    .then()
    	        .statusCode(204);

    	RestAssured.given()
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .get("/api/pettypes/2")
    	    .then()
    	        .statusCode(200)
    	        .contentType(ContentType.JSON)
    	        .body("id", equalTo(2))
    	        .body("name", equalTo("dog I"));
    }

    @Test
    void testUpdatePetTypeError() throws Exception {
    	PetType newPetType = petTypes.get(0);
    	newPetType.setName("");
    	ObjectMapper mapper = this.objectMapper;
        String newPetTypeAsJSON = mapper.writeValueAsString(petTypeMapper.toPetTypeDto(newPetType));
    	RestAssured.given()
    	        .body(newPetTypeAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .put("/api/pettypes/1")
    	    .then()
    	        .statusCode(400);
     }

    @Test
    void testDeletePetTypeSuccess() throws Exception {
    	PetType newPetType = petTypes.get(0);
    	ObjectMapper mapper = this.objectMapper;
    	String newPetTypeAsJSON = mapper.writeValueAsString(newPetType);
    	given(this.clinicService.findPetTypeById(1)).willReturn(petTypes.get(0));
    	RestAssured.given()
    	        .body(newPetTypeAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .delete("/api/pettypes/1")
    	    .then()
    	        .statusCode(204);
    }

    @Test
    void testDeletePetTypeError() throws Exception {
    	PetType newPetType = petTypes.get(0);
    	ObjectMapper mapper = this.objectMapper;
        String newPetTypeAsJSON = mapper.writeValueAsString(petTypeMapper.toPetTypeDto(newPetType));
    	given(this.clinicService.findPetTypeById(999)).willReturn(null);
    	RestAssured.given()
    	        .body(newPetTypeAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .delete("/api/pettypes/999")
    	    .then()
    	        .statusCode(404);
    }

}
