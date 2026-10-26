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

import org.springframework.samples.petclinic.rest.controller.v1.UserRestControllerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.UserMapper;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.service.UserService;

@QuarkusTest
class UserRestControllerV1Tests {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    UserMapper userMapper;

    @InjectMock
    UserService userService;

    @BeforeEach
    void initVets() {
    }

    @Test
    void testCreateUserSuccess() throws Exception {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEnabled(true);
        user.addRole("OWNER_ADMIN");
        ObjectMapper mapper = this.objectMapper;
        String newVetAsJSON = mapper.writeValueAsString(userMapper.toUserDto(user));
        RestAssured.given()
                .body(newVetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201);
    }

    @Test
    void testCreateUserError() throws Exception {
        User user = new User();
        user.setUsername(""); // set empty username to force 400 error
        user.setPassword("password");
        user.setEnabled(true);
        ObjectMapper mapper = this.objectMapper;
        String newVetAsJSON = mapper.writeValueAsString(userMapper.toUserDto(user));
        RestAssured.given()
                .body(newVetAsJSON)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post("/api/users")
            .then()
                .statusCode(400);
    }
}
