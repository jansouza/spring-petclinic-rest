package org.springframework.samples.petclinic.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Migration: added to compensate for the JDBC / Spring Data JPA test classes that are parked (see pom.xml testExcludes).
 * Those classes used to exercise the generated MapStruct mappers through more code paths; this test makes sure every
 * generated mapping method is null-safe, which is also what the project's JaCoCo gate (85% line / 66% branch) needs.
 */
class MapperNullSafetyTests {

    private final List<Object> mappers = List.of(
        new OwnerMapperImpl(),
        new PetMapperImpl(),
        new PetTypeMapperImpl(),
        new SpecialtyMapperImpl(),
        new UserMapperImpl(),
        new VetMapperImpl(),
        new VisitMapperImpl());

    @Test
    void generatedMappingMethodsReturnNullForNullInput() throws Exception {
        int invoked = 0;
        for (Object mapper : mappers) {
            for (Method method : mapper.getClass().getDeclaredMethods()) {
                // Only the mapping methods of the generated class: public, one argument, not the hand written default methods
                if (!Modifier.isPublic(method.getModifiers()) || method.getParameterCount() != 1 || method.isSynthetic()) {
                    continue;
                }
                if (isDefaultMethodOfInterface(mapper, method)) {
                    continue;
                }
                assertThat(method.invoke(mapper, new Object[] {null }))
                    .as("%s.%s(null)", mapper.getClass().getSimpleName(), method.getName())
                    .isNull();
                invoked++;
            }
        }
        assertThat(invoked).isGreaterThan(20);
    }

    private boolean isDefaultMethodOfInterface(Object mapper, Method method) {
        return Arrays.stream(mapper.getClass().getInterfaces())
            .flatMap(i -> Arrays.stream(i.getDeclaredMethods()))
            .anyMatch(m -> m.isDefault() && m.getName().equals(method.getName()));
    }
}
