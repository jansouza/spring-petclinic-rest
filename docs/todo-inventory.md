# TODO inventory: Spring Boot to Quarkus migration (run-01)

Every `TODO: Migration required` left in the code, grouped by theme. Counts were taken with grep on `migration/run-01`.
Parked sources (no TODO comment in the file itself, but excluded from compilation in `pom.xml`) are listed in section 2.

Total: 57 TODO comments in 21 files + 30 parked source files (12 JDBC, 15 Spring Data JPA, 3 security/aspect).

## Priority overview

| # | Theme | Items | Priority | Effort | Risk if ignored |
|---|-------|-------|----------|--------|-----------------|
| 1 | Authorization (`@PreAuthorize`) and Spring Security config | 36 comments + 2 parked classes | High | Medium | With `petclinic.security.enable=true` the API is unauthenticated |
| 2 | Parked persistence layers (JDBC, Spring Data JPA) | 27 parked files + 7 parked tests | Medium | High | Lost functionality (alternate repository layers); no impact on the default JPA layer |
| 3 | `CallMonitoringAspect` (AOP + JMX) | 1 parked file | Low | Low | Lost call-timing metrics |
| 4 | HSQLDB and H2 console | 5 comments | Low | n/a | Not supportable in Quarkus; remove or document |
| 5 | `spring.messages.*` bundles | 1 comment | Low | Low | Unused resource files |
| 6 | Documentation-only comments (pom.xml, exception translation) | 8 comments | Low | Low | Documentation only |

## 1. Authorization and Spring Security (High)

### 1.1 `@PreAuthorize` (36 comments)

Cause: `hasRole(@roles.X)` uses a SpEL reference to a `@roles` bean, which `quarkus-spring-security` does not support.
Default behaviour is preserved because the original also ran with security disabled (`petclinic.security.enable=false`).

| File | Count |
|------|-------|
| `rest/controller/v1/OwnerRestControllerV1.java` | 9 |
| `rest/controller/v1/PetTypeRestControllerV1.java` | 5 |
| `rest/controller/v1/SpecialtyRestControllerV1.java` | 5 |
| `rest/controller/v1/VetRestControllerV1.java` | 5 |
| `rest/controller/v1/VisitRestControllerV1.java` | 5 |
| `rest/controller/v1/PetRestControllerV1.java` | 4 |
| `rest/controller/v1/UserRestControllerV1.java` | 1 |
| `rest/controller/v2/OwnerRestControllerV2.java` | 1 |
| `rest/controller/v2/PetRestControllerV2.java` | 1 |

Expressions that were not applied:

| Expression | Count |
|------------|-------|
| `hasRole(@roles.OWNER_ADMIN)` | 20 |
| `hasRole(@roles.VET_ADMIN)` | 13 |
| `hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)` | 2 |
| `hasRole(@roles.ADMIN)` | 1 |

Suggested fix: use `quarkus-security` and `@RolesAllowed("OWNER_ADMIN")` etc. on the JAX-RS methods. Note the original role names
carry a `ROLE_` prefix (`Roles.java`: `ROLE_OWNER_ADMIN`, `ROLE_VET_ADMIN`, `ROLE_ADMIN`). Then `security/Roles.java` can go.

### 1.2 Spring Security configuration (2 parked classes)

| File | What it did | Suggested fix |
|------|-------------|---------------|
| `security/BasicAuthenticationConfig.java` | `SecurityFilterChain` with HTTP Basic, CSRF disabled, `anyRequest().authenticated()` | `quarkus-security-jpa` (or `quarkus-elytron-security-properties-file`) with `quarkus.http.auth.basic=true`, controlled by `petclinic.security.enable` |
| `security/DisableSecurityConfig.java` | permit-all filter chain | Not needed: Quarkus is open when no policy is configured |

Related: `UserRestControllerV1` creates users and `UserServiceImpl` stores the password as received. That is pre-existing behaviour, but
it should be fixed together with this item (hash passwords with `BcryptUtil` when authentication is enabled).

## 2. Parked persistence layers (Medium)

Cause: the `quarkus-spring-data-jpa` 3.40.1 processor cannot handle the `XRepository` / `SpringDataXRepository` / `*Override` design
(decision D4), and `JdbcTemplate` has no compat extension. The plain-JPA implementation is the active layer.

| Location | Files | Notes |
|----------|-------|-------|
| `repository/jdbc/**` | 12 | Spring `JdbcTemplate`, `NamedParameterJdbcTemplate`, `SimpleJdbcInsert`. Needs a rewrite with Agroal `DataSource` + plain JDBC, or deletion. |
| `repository/springdatajpa/**` | 15 | Possible fix: rewrite as one Spring Data interface per entity, with the custom `save`/`delete` as default methods or via Panache. Alternatively delete, since the JPA layer covers the same functionality. |
| `security/BasicAuthenticationConfig.java`, `DisableSecurityConfig.java` | 2 | See 1.2 |
| `util/CallMonitoringAspect.java` | 1 | See 3 |

Parked tests (each has a `TODO: Migration required` comment and is listed in `testExcludes` in `pom.xml`):

| File |
|------|
| `service/clinicService/ClinicServiceH2JdbcTests.java` |
| `service/clinicService/ClinicServiceHsqlJdbcTests.java` |
| `service/clinicService/ClinicServiceSpringDataJpaTests.java` |
| `service/clinicService/ApplicationTestConfig.java` |
| `service/userService/UserServiceH2JdbcTests.java` |
| `service/userService/UserServiceHsqlJdbcTests.java` |
| `service/userService/UserServiceSpringDataJpaTests.java` |

Decision to take: keep the parked layers as a record for the original design, or delete them (and the tests, the `testExcludes` and the
`excludes` in `pom.xml`) once the team confirms that only JPA is needed. If kept, there is a maintenance cost: they no longer compile.

## 3. `CallMonitoringAspect` (Low)

`util/CallMonitoringAspect.java` measured call counts and timings with AspectJ and exposed them over Spring JMX. Suggested fix: a CDI
`@Interceptor` plus a Micrometer timer (`quarkus-micrometer`), or drop it.

## 4. HSQLDB and H2 console (Low)

Not supportable in Quarkus (no `quarkus-jdbc-hsqldb`, no H2 console). Suggested fix: remove `db/hsqldb/**`, `application-hsqldb.properties`
and the TODO comments, or keep them only as documentation.

| File | Line |
|------|------|
| `src/main/resources/application.properties` | 12 |
| `src/main/resources/application-hsqldb.properties` | 1 |
| `src/main/resources/application-h2.properties` | 13 (H2 console) |
| `pom.xml` | 138, 140 |

## 5. Messages bundles (Low)

| File | Line | Item |
|------|------|------|
| `src/main/resources/application.properties` | 45 | `spring.messages.basename=messages/messages` |

No Java code references `messages/*`, so the bundles in `src/main/resources/messages` can probably be deleted. Confirm with the team.

## 6. Documentation-only comments in `pom.xml` and `ExceptionControllerAdvice` (Low)

| File | Line | Item |
|------|------|------|
| `pom.xml` | 34 | `spring-data-jdbc.version` property, only used by a commented-out dependency |
| `pom.xml` | 118 | `spring-boot-starter-cache` removed (no caching used) |
| `pom.xml` | 120 | `spring-boot-starter-aspectj` removed (see 3) |
| `pom.xml` | 122 | `spring-boot-starter-jdbc` removed (see 2) |
| `pom.xml` | 141 | `spring-data-jdbc-core` commented out (see 2) |
| `pom.xml` | 447 | compiler `excludes` list (see 2) |
| `pom.xml` | 464 | `testExcludes` list (see 2) |
| `rest/advice/ExceptionControllerAdvice.java` | 102 | `DataIntegrityViolationException` was raised by Spring's exception translation; `PersistenceException` is mapped instead |

These TODOs disappear when the corresponding item in sections 1 to 5 is resolved.

## Not tracked with a TODO comment, but open (see `migration-report.md`)

- `GET /petclinic/` returns 404 (original redirected to Swagger UI).
- `POST /api/vets` with specialties returned `specialties: []` in the smoke test (not compared against the original).
- `/api/oops` has no implementation (pre-existing).
- `quarkus.hibernate-orm.database.generation` is flagged as deprecated; `@Valid` on containers in generated DTOs logs `HV000271`.
- Spring `@Transactional(readOnly = true)` is ignored by `quarkus-spring-tx` (19 methods in `ClinicServiceImpl`).
