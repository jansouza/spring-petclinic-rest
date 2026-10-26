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

import org.springframework.samples.petclinic.rest.controller.v1.VisitRestControllerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;

/**
 * Test class for {@link VisitRestControllerV1}
 *
 * @author Vitaliy Fedoriv
 */
@QuarkusTest
class VisitRestControllerV1Tests {

    @InjectMock
    ClinicService clinicService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    VisitMapper visitMapper;

    private List<Visit> visits;

    @BeforeEach
    void initVisits(){

        visits = new ArrayList<>();

    	Owner owner = new Owner();
    	owner.setId(1);
    	owner.setFirstName("Eduardo");
    	owner.setLastName("Rodriquez");
    	owner.setAddress("2693 Commerce St.");
    	owner.setCity("McFarland");
    	owner.setTelephone("6085558763");

    	PetType petType = new PetType();
    	petType.setId(2);
    	petType.setName("dog");

    	Pet pet = new Pet();
    	pet.setId(8);
    	pet.setName("Rosy");
        pet.setBirthDate(LocalDate.now());
    	pet.setOwner(owner);
    	pet.setType(petType);

    	Visit visit = new Visit();
    	visit.setId(2);
    	visit.setPet(pet);
        visit.setDate(LocalDate.now());
    	visit.setDescription("rabies shot");
    	visits.add(visit);

    	visit = new Visit();
    	visit.setId(3);
    	visit.setPet(pet);
        visit.setDate(LocalDate.now());
    	visit.setDescription("neutered");
    	visits.add(visit);

    }

    @Test
    void testGetVisitSuccess() throws Exception {
    	given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/visits/2")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(2))
                .body("description", equalTo("rabies shot"));
    }

    @Test
    void testGetVisitNotFound() throws Exception {
        given(this.clinicService.findVisitById(999)).willReturn(null);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/visits/999")
            .then()
                .statusCode(404);
    }

    @Test
    void testGetAllVisitsSuccess() throws Exception {
    	given(this.clinicService.findAllVisits()).willReturn(visits);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/visits")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("[0].id", equalTo(2))
                .body("[0].description", equalTo("rabies shot"))
                .body("[1].id", equalTo(3))
                .body("[1].description", equalTo("neutered"));
    }

    @Test
    void testGetAllVisitsNotFound() throws Exception {
    	visits.clear();
    	given(this.clinicService.findAllVisits()).willReturn(visits);
        RestAssured.given()
                .accept(ContentType.JSON)
            .when()
                .get("/api/visits")
            .then()
                .statusCode(404);
    }

    @Test
    void testCreateVisitSuccess() throws Exception {
    	Visit newVisit = visits.get(0);
    	newVisit.setId(999);
    	ObjectMapper mapper = this.objectMapper;
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
    	System.out.println("newVisitAsJSON " + newVisitAsJSON);
    	RestAssured.given()
    	        .body(newVisitAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .post("/api/visits")
    	    .then()
    	        .statusCode(201);
    }

    @Test
    void testCreateVisitError() throws Exception {
    	Visit newVisit = visits.get(0);
    	newVisit.setId(null);
        newVisit.setDescription(null);
    	ObjectMapper mapper = this.objectMapper;
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
    	RestAssured.given()
    	        .body(newVisitAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .post("/api/visits")
    	    .then()
    	        .statusCode(400);
     }

    @Test
    void testUpdateVisitSuccess() throws Exception {
    	given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
    	Visit newVisit = visits.get(0);
    	newVisit.setDescription("rabies shot test");
    	ObjectMapper mapper = this.objectMapper;
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
    	RestAssured.given()
    	        .body(newVisitAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .put("/api/visits/2")
    	    .then()
    	        .statusCode(204);

    	RestAssured.given()
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .get("/api/visits/2")
    	    .then()
    	        .statusCode(200)
    	        .contentType(ContentType.JSON)
    	        .body("id", equalTo(2))
    	        .body("description", equalTo("rabies shot test"));
    }

    @Test
    void testUpdateVisitError() throws Exception {
    	Visit newVisit = visits.get(0);
        newVisit.setDescription(null);
    	ObjectMapper mapper = this.objectMapper;
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
    	RestAssured.given()
    	        .body(newVisitAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .put("/api/visits/2")
    	    .then()
    	        .statusCode(400);
     }

    @Test
    void testDeleteVisitSuccess() throws Exception {
    	Visit newVisit = visits.get(0);
    	ObjectMapper mapper = this.objectMapper;
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
    	given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
    	RestAssured.given()
    	        .body(newVisitAsJSON)
    	        .accept(ContentType.JSON)
    	        .contentType(ContentType.JSON)
    	    .when()
    	        .delete("/api/visits/2")
    	    .then()
    	        .statusCode(204);
    }

    @Test
    void testDeleteVisitError() throws Exception {
    	Visit newVisit = visits.get(0);
    	ObjectMapper mapper = this.objectMapper;
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
        given(this.clinicService.findVisitById(999)).willReturn(null);
        RestAssured.given()
                .body(newVisitAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .delete("/api/visits/999")
            .then()
                .statusCode(404);
    }

}
