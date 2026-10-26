## Migration Report: spring-petclinic-rest

### Summary
- Strategy: Spring Compatibility (hybrid: the REST layer is full Quarkus/JAX-RS, see D1 in `docs/quarkus-migration-decisions.md`)
- Agent: Bob (IBM Bob, Java Modernization mode)
- Model: not exposed in the session context
- Modules completed: 6/6 (jdk, build, code, testing, cleanup completed; frontend SKIPPED because there is no view layer)
- Checks passed: 6/6 (plus `mvnw.cmd clean verify` including the JaCoCo gate, see "What checks failed initially")
- Token usage: not available (session statistics are not exposed to the agent)
- Estimated cost: not available
- From: Spring Boot 4.1.1 / Maven / JDK 21. To: Quarkus 3.40.1 / Maven / JDK 21 (app version 4.0.2)

### Changes by Module
| Module | Files changed | Key changes |
|--------|--------------|-------------|
| jdk | none | JDK 21 already in use; gate passed. |
| build | `pom.xml`, `application.properties`, `application-{h2,hsqldb,mysql,postgres}.properties`, `.gitignore` | Spring Boot parent replaced by the Quarkus BOM 3.40.1 and `quarkus-maven-plugin`. Extensions: `quarkus-rest-jackson`, `quarkus-spring-di`, `quarkus-spring-tx`, `quarkus-spring-data-jpa`, `quarkus-hibernate-validator`, `quarkus-smallrye-health`, `quarkus-smallrye-openapi`, `quarkus-jdbc-{h2,mysql,postgresql}`. `openapi-generator` switched to `jaxrs-spec` (`generateJsonCreator=false`). MapStruct uses the `jakarta-cdi` component model. Spring properties mapped to Quarkus equivalents (root path `/petclinic/`, port 9966, CORS allowlist, host `127.0.0.1`, Hibernate `drop-and-create` plus `sql-load-script`). Parked sources are excluded in `maven-compiler-plugin`. |
| code | `PetClinicApplication`, `config/SwaggerConfig`, `model/Owner`, `repository/*Repository` (7), `repository/jpa/*` (7), `rest/advice/ExceptionControllerAdvice`, `rest/controller/BindingErrorsResponse`, `rest/controller/v1/*` (8), `rest/controller/v2/*` (2), `service/*Impl` (2), `util/EntityUtils` | `@SpringBootApplication` replaced by a JAX-RS `Application` (`@ApplicationPath("/api")`). Controllers implement the generated JAX-RS interfaces. `@ControllerAdvice` replaced by `@ServerExceptionMapper`. The JPA repositories are `@ApplicationScoped` with an injected `EntityManager`. Services are `@ApplicationScoped`. OpenAPI customisation via `@OpenAPIDefinition`. The root redirect is a Vert.x route. |
| frontend | none | SKIPPED: no `templates/` or `static/` resources. |
| testing | 20 test classes, `MapperNullSafetyTests` (new), `src/test/resources/application.properties` (removed), `pom.xml` (test deps, JaCoCo) | 8 MockMvc controller tests converted to `@QuarkusTest` + REST-Assured + `@InjectMock`. Service tests converted to `@QuarkusTest` + `@TestTransaction`. `ValidatorTests` uses plain Jakarta Validation. AssertJ 3.27.7 and `quarkus-jacoco` added as test dependencies; JaCoCo reconfigured for the `QuarkusClassLoader` (D17). 127 tests run, 0 failures. |
| cleanup | `pom.xml`, `application.properties`, `logback.xml` (removed), `readme.md`, `service/*Impl` | `quarkus-spring-web` and `quarkus-spring-security` removed. `logback.xml` removed. Readme rewritten for Quarkus (run commands, health, Swagger UI, database profiles). Docs updated (`docs/quarkus-migration-decisions.md`, D1-D18). |

### Validation Results
| Check | Result | Notes |
|-------|--------|-------|
| Builds | PASS | `mvnw.cmd clean package -DskipTests`: 0 errors. Warnings only: MapStruct `remove*Item` unmapped targets, Spring `@Transactional(readOnly)` ignored by `quarkus-spring-tx`. |
| No Spring deps | PASS | `pom.xml` has no `org.springframework` dependency. The only matches are the project's own `groupId` (`org.springframework.samples`), the generated-package names and a commented-out block. `Page`/`Pageable`/`PageImpl` come from `quarkus-spring-data-commons-api` (a transitive dependency of `quarkus-spring-data-jpa`). |
| Has Quarkus | PASS | Quarkus BOM 3.40.1 plus the extensions above. |
| Tests pass | PASS | `mvnw.cmd clean verify`: Tests run: 127, Failures: 0, Errors: 0, Skipped: 0. The original build ran 237; the 111 missing tests belong to the parked JDBC / Spring Data JPA / HSQLDB layers (see `docs/test-coverage.md`). |
| Coverage gate (`jacoco:check`) | PASS after fix | 88.8% lines (min 85%), 76.0% branches (min 66%). Original build: 85.5% / 66.4%. It failed at 10% / 5% right after the migration, see below. |
| Starts up | PASS | `java -jar target/quarkus-app/quarkus-run.jar` starts in about 2.8 s, listens on `127.0.0.1:9966`, `GET /petclinic/q/health` returns `UP` and `GET /petclinic/api/owners` returns 200. Earlier in the run a REST smoke test across all resources (GET/POST/PUT/DELETE, 404, 400) passed. |
| No leftover templates | PASS | No Thymeleaf/JSP references and no template files. |

### What migrated cleanly
- DI, `@Transactional` (service layer) through `quarkus-spring-di` / `quarkus-spring-tx`.
- JPA entities, Bean Validation constraints, MapStruct mappers (switched to the `jakarta-cdi` component model).
- The `ClinicService` integration tests (36 tests) ran unchanged apart from annotations.

### What required manual judgment
- **REST layer (D1):** `quarkus-spring-web` ignores mappings inherited from generated interfaces, so the generator was switched to `jaxrs-spec` instead of duplicating annotations in each controller.
- **Persistence layer (D4):** `quarkus-spring-data-jpa` 3.40.1 cannot process the project's `XRepository` / `SpringDataXRepository` / `*Override` design, so the existing plain-JPA implementation was made the active layer.
- **Services:** `@Service` produces a `@Singleton`, which `@InjectMock` cannot mock; the beans are now `@ApplicationScoped`.
- **CORS / bind address (D9, D10):** the original allowed all origins; an explicit allowlist and a `127.0.0.1` bind are used instead, per the project security rules.
- **Test conversion (D15):** MockMvc to REST-Assured against a running `@QuarkusTest` application, with services mocked.

### Unmigrated Code (TODOs)
| File | Line | What | Why not migrated |
|------|------|------|-----------------|
| `rest/controller/v1/*RestControllerV1.java`, `rest/controller/v2/*RestControllerV2.java` (10 files, 36 occurrences in total) | various | `@PreAuthorize("hasRole(@roles.X)")` | SpEL with a `@roles` bean is not supported by `quarkus-spring-security`. Security is off by default, so default behaviour is preserved. |
| `security/BasicAuthenticationConfig.java`, `security/DisableSecurityConfig.java` | all | Spring Security `SecurityFilterChain` | No equivalent; excluded from compilation. Should be replaced by Quarkus security (`quarkus-security`, basic auth) if authentication is needed. |
| `security/Roles.java` | all | Spring `@Component` | Unchanged; only referenced from the parked SpEL. |
| `repository/jdbc/**` | all | Spring `JdbcTemplate` repositories | No Quarkus compat extension; excluded from compilation. |
| `repository/springdatajpa/**` | all | Spring Data JPA repositories | The `quarkus-spring-data-jpa` processor cannot handle this design (see D4); excluded from compilation. |
| `util/CallMonitoringAspect.java` | all | AspectJ + Spring JMX | No compat equivalent; could become a CDI interceptor. |
| `ClinicService{H2Jdbc,HsqlJdbc,SpringDataJpa}Tests`, `UserService{H2Jdbc,HsqlJdbc,SpringDataJpa}Tests`, `ApplicationTestConfig` | all | Tests of parked layers / Spring test config | Kept with TODO and listed in `testExcludes`. |
| `application.properties` | - | `spring.messages.basename` (`messages/*`) | No Spring `MessageSource` in Quarkus; the bundles are kept until their use is confirmed. No Java code references them. |
| `application-hsqldb.properties`, `db/hsqldb/**` | - | HSQLDB profile | No Quarkus JDBC extension for HSQLDB. |
| `rest/advice/ExceptionControllerAdvice.java` | 102 | `DataIntegrityViolationException` translation | Spring's exception translation no longer exists; `PersistenceException` is mapped instead. |

### Removed Code
| File | What was removed | Justification |
|------|-----------------|---------------|
| `PetClinicApplication.java` | `@SpringBootApplication`, `SpringBootServletInitializer`, `SpringApplication.run(...)` | Quarkus has its own bootstrap (marked with a `// REMOVED:` comment). |
| `src/main/resources/logback.xml` | Whole file | Quarkus does not use Logback; its only setting was moved to `application.properties`. |
| `src/test/resources/application.properties` | Whole file | Spring-only profile/property configuration. The `src/main` Quarkus configuration is used in tests. |
| `pom.xml` | `spring-boot-*` starters, `springdoc`, `spring-data-jdbc-core`, `hsqldb`, Jackson 3 (`tools.jackson`) | Replaced by the Quarkus extensions or parked (commented with TODO in the `pom.xml`). |

### Runtime risks and known behaviour differences
- **Security:** with `petclinic.security.enable=true` no authentication is enforced any more (see the TODOs above).
- **Repository-layer selection:** the `jpa` / `jdbc` / `spring-data-jpa` runtime choice is gone; only JPA is available.
- **Database:** `quarkus.datasource.db-kind` is a build-time property, so MySQL/PostgreSQL need a build with the matching profile. The schema is created by Hibernate (`drop-and-create`), which is suitable for the in-memory default only.
- **`GET /petclinic/`** returns 404; the original redirected to Swagger UI. The Vert.x route in `RootRestControllerV1` is not effective. Minor; to be investigated.
- **`/api/oops`** returns 404; `OopsApi` has no implementation (the Spring code had none either).
- **Validation error `title`** is now `ResteasyReactiveViolationException` (was `MethodArgumentNotValidException`); the body is still `application/problem+json`.
- **`POST /api/vets` with specialties:** the earlier smoke-test concern was not reproduced; the request had sent an empty list (see `docs/api-contract-comparison.md`).
- **Startup warnings:** Hibernate Validator `HV000271` (`@Valid` on containers in the generated DTOs), and `quarkus.hibernate-orm.database.generation` being flagged as deprecated.

### What checks failed initially
- Controller tests: `@InjectMock` failed on `@Service` beans (`@Singleton`, not a normal scope) -> `@ApplicationScoped`.
- `UserServiceJpaTests` failed with a unique-key violation on `roles` outside a transaction -> `@TestTransaction` (the transactional rollback that Spring's `@Transactional` provided).
- `PetRestControllerV1Tests` still referenced `@MockitoBean` -> `@InjectMock`, and a mock stub on a real mapper was corrected.
- Earlier in the run: POST of readOnly `id` DTOs returned 400 -> `generateJsonCreator=false`; Spring Data JPA processor failures -> D4.
- **`mvnw.cmd clean verify` failed on the JaCoCo gate** (lines 0.10 against a 0.85 minimum, branches 0.05 against 0.66), although the 126 tests passed. `@QuarkusTest` loads the application through the `QuarkusClassLoader`, which the `prepare-agent` agent does not record. Fix (D17): `quarkus-jacoco` test dependency; `jacoco-maven-plugin` excludes `*QuarkusClassLoader` and both writers share `target/jacoco-quarkus.exec`. That reached 84.9% / 64.5%, still short, so `MapperNullSafetyTests` was added (D18). Final: 88.8% lines, 76.0% branches. The thresholds were not changed. Details: `docs/test-coverage.md`.

### Additional reports
- `docs/todo-inventory.md`: all 57 `TODO: Migration required` comments in 21 files, and the 30 parked source files.
- `docs/api-contract-comparison.md`: `openapi.yml` against the running application (36 of 37 operations served; differences listed).
- `docs/test-coverage.md`: tests and coverage, original against migrated.

### Skill Improvement Suggestions
- Document that `quarkus-spring-web` does not pick up mappings declared on generated interfaces (OpenAPI Generator `spring` interfaceOnly), and recommend `jaxrs-spec`.
- Document the `quarkus-spring-data-jpa` limitations with repository interfaces that extend non-Spring-Data parents or redeclare `save`/`delete`.
- Add mappings: `@Service` -> normal scope for `@InjectMock`; `@MockitoBean` -> `io.quarkus.test.InjectMock`; `MockMvc` -> REST-Assured; `@Transactional` tests -> `@TestTransaction`; AssertJ is not part of the Quarkus test starters.
- Add the `quarkus.http.host` default (0.0.0.0 in prod) to the config map for projects with a "no all-interfaces binding" policy.
- Add a note that `readOnly` on Spring `@Transactional` is ignored.
