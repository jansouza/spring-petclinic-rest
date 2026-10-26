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

import org.springframework.samples.petclinic.rest.controller.v1.SpecialtyRestControllerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.service.ClinicService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;

/**
 * Test class for {@link SpecialtyRestControllerV1}
 *
 * @author Vitaliy Fedoriv
 */
@QuarkusTest
class SpecialtyRestControllerV1Tests {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SpecialtyMapper specialtyMapper;

	@InjectMock
	ClinicService clinicService;

    private List<Specialty> specialties;

    @BeforeEach
    void initSpecialtys(){
    	specialties = new ArrayList<Specialty>();

    	Specialty specialty = new Specialty();
    	specialty.setId(1);
    	specialty.setName("radiology");
    	specialties.add(specialty);

    	specialty = new Specialty();
    	specialty.setId(2);
    	specialty.setName("surgery");
    	specialties.add(specialty);

    	specialty = new Specialty();
    	specialty.setId(3);
    	specialty.setName("dentistry");
    	specialties.add(specialty);

    }

    @Test
    void testGetSpecialtySuccess() throws Exception {
    	given(this.clinicService.findSpecialtyById(1)).willReturn(specialties.get(0));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/specialties/1")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
                .body("name", equalTo("radiology"));
    }

    @Test
    void testGetSpecialtyNotFound() throws Exception {
    	given(this.clinicService.findSpecialtyById(999)).willReturn(null);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/specialties/999")
            .then()
                .statusCode(404);
    }

    @Test
    void testGetAllSpecialtysSuccess() throws Exception {
    	specialties.remove(0);
    	given(this.clinicService.findAllSpecialties()).willReturn(specialties);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/specialties")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[0].id", equalTo(2))
                .body("[0].name", equalTo("surgery"))
                .body("[1].id", equalTo(3))
                .body("[1].name", equalTo("dentistry"));
    }

    @Test
    void testGetAllSpecialtysNotFound() throws Exception {
    	specialties.clear();
    	given(this.clinicService.findAllSpecialties()).willReturn(specialties);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/specialties")
            .then()
                .statusCode(404);
    }

    @Test
    void testCreateSpecialtySuccess() throws Exception {
    	Specialty newSpecialty = specialties.get(0);
    	newSpecialty.setId(999);
    	ObjectMapper mapper = this.objectMapper;
        String newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty));
    	RestAssured.given()
    	        .body(newSpecialtyAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .post("/api/specialties")
    	    .then()
    	        .statusCode(201);
    }

    @Test
    void testCreateSpecialtyError() throws Exception {
    	Specialty newSpecialty = specialties.get(0);
    	newSpecialty.setId(null);
    	newSpecialty.setName(null);
    	ObjectMapper mapper = this.objectMapper;
        String newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty));
    	RestAssured.given()
    	        .body(newSpecialtyAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .post("/api/specialties")
    	    .then()
    	        .statusCode(400);
     }

    @Test
    void testUpdateSpecialtySuccess() throws Exception {
    	given(this.clinicService.findSpecialtyById(2)).willReturn(specialties.get(1));
    	Specialty newSpecialty = specialties.get(1);
    	newSpecialty.setName("surgery I");
    	ObjectMapper mapper = this.objectMapper;
        String newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty));
    	RestAssured.given()
    	        .body(newSpecialtyAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .put("/api/specialties/2")
    	    .then()
    	        .statusCode(204);

    	RestAssured.given()
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .get("/api/specialties/2")
    	    .then()
    	        .statusCode(200)
    	        .contentType(ContentType.JSON)
    	        .body("id", equalTo(2))
    	        .body("name", equalTo("surgery I"));
    }

    @Test
    void testUpdateSpecialtyError() throws Exception {
    	Specialty newSpecialty = specialties.get(0);
    	newSpecialty.setName("");
    	ObjectMapper mapper = this.objectMapper;
        String newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty));
    	RestAssured.given()
    	        .body(newSpecialtyAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .put("/api/specialties/1")
    	    .then()
    	        .statusCode(400);
     }

    @Test
    void testDeleteSpecialtySuccess() throws Exception {
    	Specialty newSpecialty = specialties.get(0);
    	ObjectMapper mapper = this.objectMapper;
        String newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty));
    	given(this.clinicService.findSpecialtyById(1)).willReturn(specialties.get(0));
    	RestAssured.given()
    	        .body(newSpecialtyAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .delete("/api/specialties/1")
    	    .then()
    	        .statusCode(204);
    }

    @Test
    void testDeleteSpecialtyError() throws Exception {
    	Specialty newSpecialty = specialties.get(0);
    	ObjectMapper mapper = this.objectMapper;
        String newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty));
    	given(this.clinicService.findSpecialtyById(999)).willReturn(null);
    	RestAssured.given()
    	        .body(newSpecialtyAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .delete("/api/specialties/999")
    	    .then()
    	        .statusCode(404);
    }
}
