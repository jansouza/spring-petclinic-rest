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

import org.springframework.samples.petclinic.rest.controller.v1.VetRestControllerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.service.ClinicService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;

/**
 * Test class for {@link VetRestControllerV1}
 *
 * @author Vitaliy Fedoriv
 */
@QuarkusTest
class VetRestControllerV1Tests {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    VetMapper vetMapper;

	@InjectMock
	ClinicService clinicService;

    private List<Vet> vets;

    @BeforeEach
    void initVets(){
    	vets = new ArrayList<Vet>();

    	Vet vet = new Vet();
    	vet.setId(1);
    	vet.setFirstName("James");
    	vet.setLastName("Carter");
    	vets.add(vet);

    	vet = new Vet();
    	vet.setId(2);
    	vet.setFirstName("Helen");
    	vet.setLastName("Leary");
    	vets.add(vet);

    	vet = new Vet();
    	vet.setId(3);
    	vet.setFirstName("Linda");
    	vet.setLastName("Douglas");
    	vets.add(vet);
    }

    @Test
    void testGetVetSuccess() throws Exception {
    	given(this.clinicService.findVetById(1)).willReturn(vets.get(0));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/vets/1")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
                .body("firstName", equalTo("James"));
    }

    @Test
    void testGetVetNotFound() throws Exception {
    	given(this.clinicService.findVetById(999)).willReturn(null);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/vets/999")
            .then()
                .statusCode(404);
    }

    @Test
    void testGetAllVetsSuccess() throws Exception {
    	given(this.clinicService.findAllVets()).willReturn(vets);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/vets")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[0].id", equalTo(1))
                .body("[0].firstName", equalTo("James"))
                .body("[1].id", equalTo(2))
                .body("[1].firstName", equalTo("Helen"));
    }

    @Test
    void testGetAllVetsNotFound() throws Exception {
    	vets.clear();
    	given(this.clinicService.findAllVets()).willReturn(vets);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/vets")
            .then()
                .statusCode(404);
    }

    @Test
    void testCreateVetSuccess() throws Exception {
    	Vet newVet = vets.get(0);
    	newVet.setId(999);
    	ObjectMapper mapper = this.objectMapper;
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	RestAssured.given()
    	        .body(newVetAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .post("/api/vets")
    	    .then()
    	        .statusCode(201);
    }

    @Test
    void testCreateVetError() throws Exception {
    	Vet newVet = vets.get(0);
    	newVet.setId(null);
    	newVet.setFirstName(null);
    	ObjectMapper mapper = this.objectMapper;
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	RestAssured.given()
    	        .body(newVetAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .post("/api/vets")
    	    .then()
    	        .statusCode(400);
     }

    @Test
    void testUpdateVetSuccess() throws Exception {
    	given(this.clinicService.findVetById(1)).willReturn(vets.get(0));
    	Vet newVet = vets.get(0);
    	newVet.setFirstName("James");
    	ObjectMapper mapper = this.objectMapper;
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	RestAssured.given()
    	        .body(newVetAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .put("/api/vets/1")
    	    .then()
    	        .statusCode(204);

    	RestAssured.given()
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .get("/api/vets/1")
    	    .then()
    	        .statusCode(200)
    	        .contentType(ContentType.JSON)
    	        .body("id", equalTo(1))
    	        .body("firstName", equalTo("James"));

    }

    @Test
    void testUpdateVetError() throws Exception {
    	Vet newVet = vets.get(0);
    	newVet.setFirstName(null);
    	ObjectMapper mapper = this.objectMapper;
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	RestAssured.given()
    	        .body(newVetAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .put("/api/vets/1")
    	    .then()
    	        .statusCode(400);
     }

    @Test
    void testDeleteVetSuccess() throws Exception {
    	Vet newVet = vets.get(0);
    	ObjectMapper mapper = this.objectMapper;
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	given(this.clinicService.findVetById(1)).willReturn(vets.get(0));
    	RestAssured.given()
    	        .body(newVetAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .delete("/api/vets/1")
    	    .then()
    	        .statusCode(204);
    }

    @Test
    void testDeleteVetError() throws Exception {
    	Vet newVet = vets.get(0);
    	ObjectMapper mapper = this.objectMapper;
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	given(this.clinicService.findVetById(999)).willReturn(null);
    	RestAssured.given()
    	        .body(newVetAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .delete("/api/vets/999")
    	    .then()
    	        .statusCode(404);
    }

}
