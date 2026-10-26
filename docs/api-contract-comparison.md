# API contract comparison: Spring Boot (before) vs Quarkus (after), run-01

**Verdict: the HTTP contract that clients see is preserved.** All 37 operations of `src/main/resources/openapi.yml` are served at the
same paths with the same methods, request payloads and success status codes, except `GET /oops` (see 3.1). The differences
are in the *published* OpenAPI document (`/q/openapi`), which is now less complete than `openapi.yml`, and in a few details listed below.

## 1. Method

| Item | Detail |
|------|--------|
| Contract (before) | `src/main/resources/openapi.yml` (OpenAPI 3.0.1, unchanged by the migration: `git diff` is empty). The Spring version generated its controllers from it with `openapi-generator` (`spring`) and published it through springdoc. |
| Contract (after) | `GET /petclinic/q/openapi?format=json` from the packaged Quarkus app (`quarkus-smallrye-openapi`), 28,417 bytes. |
| Behaviour (after) | A script called 35 of the 37 operations on the running app (valid, invalid and unknown-id cases) and compared status code, `Content-Type` and `Location` with the status codes declared in `openapi.yml`. The remaining 2 were checked by hand (`POST /visits`, `GET /oops`). |
| Behaviour (before) | Not run. The Spring build was not started; "before" behaviour is taken from `openapi.yml`, the original controllers in `HEAD` and the original MockMvc tests in `HEAD`. |

The comparison scripts lived in `%TEMP%` and are not part of the repository.

## 2. Results at a glance

| Check | Result |
|-------|--------|
| Paths and methods | 36 of 37 operations present. Missing: `GET /oops` (3.1). The runtime paths carry the `/petclinic/api` prefix that `openapi.yml` keeps in `servers`. |
| Parameters (path, query) | Identical for all 36 operations. |
| Request bodies | Same media type (`application/json`) and same payload shape; the schema names carry a `Dto` suffix at runtime (`OwnerFields` is now `OwnerFieldsDto`). |
| Success status codes seen at runtime | Match the original controllers and the original tests (201 for POST, 204 for PUT and DELETE, 200 for GET). |
| `Location` header on 201 | Present on every POST that returns an entity, with the same value format (`/api/<resource>/<id>`, no host). `POST /users` returns none, as before. |
| Error body | `application/problem+json` on 400, same type as before. Field `title` changed (3.3). |
| Runtime responses vs the codes declared in `openapi.yml` | 23 of the 38 responses recorded by the script are declared. The 15 that are not are PUT, DELETE and POST successes where the declaration in `openapi.yml` disagrees with the original code too (3.2). |

## 3. Differences

### 3.1 `GET /oops` is not served (pre-existing)

`openapi.yml` declares `GET /oops` (`failingRequest`, "Always fails", "Produces sample error response"). The generated `OopsApi` interface has
no implementing resource, so the call returns 404. The original Spring project had no controller for it either (decision D14), so
this is not a regression. To restore the documented behaviour, add a resource whose method throws an exception, which the existing
mapper turns into a 500 problem+json.

### 3.2 `openapi.yml` declares different success codes than the code returns (pre-existing, unchanged)

The original Spring controllers returned **204 No Content** from `PUT` and `DELETE` (for example `HttpStatus.NO_CONTENT` in `OwnerRestControllerV1:124,136`)
and the original tests asserted `isNoContent()`. `openapi.yml` declares 200 for most of them. The migrated app keeps the code behaviour (204), so
clients that worked against the Spring build keep working. This mismatch is in the specification and was there before the migration.

| Operation | Declared in `openapi.yml` | Returned (before and after) |
|-----------|---------------------------|------------------------------|
| `PUT` owners, pets, visits, pettypes, specialties, vets | 200 (some also 304); `PUT .../pets/{petId}` under owners declares 204 | 204 |
| `DELETE` owners, pets, visits, pettypes, specialties, vets | 200 | 204 |
| `POST /pettypes`, `/specialties`, `/vets`, `/users`, `/visits` | 200 | 201 |

### 3.3 Validation error `title` changed

| | Before | After |
|-|--------|-------|
| `title` | `MethodArgumentNotValidException` | `ResteasyReactiveViolationException` |
| `Content-Type` | `application/problem+json` | `application/problem+json` |
| Status | 400 | 400 |

Clients that branch on `title` (unlikely) would need to change. Clients that use `status` or `schemaValidationErrors` are not affected.
The `title` could be pinned to the old string in `ExceptionControllerAdvice` if exact compatibility is required.

### 3.4 Published OpenAPI document (`/q/openapi`) is less complete than the source contract

This only affects documentation and generated client SDKs, not runtime behaviour.

| Aspect | `openapi.yml` / springdoc (before) | `/q/openapi` (after) |
|--------|------------------------------------|----------------------|
| Operations | 37 | 36 (no `GET /oops`) |
| `operationId` | present (`listOwners`, ...) | **missing** on all 36 operations |
| Response declarations | per-operation codes (200/201/204/304/400/404/500) with schemas | a single `200 OK` with an empty schema (`"schema": {}`) for every operation, because the methods return `jakarta.ws.rs.core.Response` |
| Component schemas | 17 | 12. Absent: `Owner`, `OwnerPage`, `PetPage`, `ProblemDetail`, `ValidationMessage`, `VetFields` (they are not referenced by any request body) |
| `Pet`, `PetType`, `Vet`, `Visit` | detailed | present under `*Dto` names |
| `birthDate`, `date` | `string`/`date` | `$ref LocalDate` (`string`, `date`, example `2022-03-10`), which is equivalent |
| `Specialty.id` | `readOnly: true` | not `readOnly` |
| `info` | title and version from `openapi.yml` | title `spring-petclinic-rest API`, version `4.0.2` (from the Maven project) |
| `servers` | `http://localhost:9966/petclinic/api` | none (the prefix is inside each path) |
| Tags | `useTags` groups | none |

Cause: the generator was switched from `spring` (with springdoc annotations) to `jaxrs-spec` with `useSwaggerAnnotations=false` (decision D1), and the
resources return `Response`. Suggested follow-ups, in order of value:

1. Publish the original `openapi.yml` as the document: put it at `META-INF/openapi.yaml` and set `mp.openapi.scan.disable=true`, so the documented
   contract is exactly the reviewed one. (Not tried in this run; verify the result with `/q/openapi`.)
2. Alternatively set `useSwaggerAnnotations=true` in the generator so operation ids, responses and tags are generated into the interfaces.

### 3.5 Content type charset

JSON responses now carry `application/json;charset=UTF-8` (before: `application/json`). The original tests asserted
`content().contentType("application/json")`; the migrated tests assert `ContentType.JSON` with REST-Assured, which accepts the charset.
Clients that compare the header as an exact string would see a difference.

### 3.6 Behaviours observed that need a decision

| Observation | Assessment |
|-------------|------------|
| `PUT /vets/{id}` with `firstName: "A2"` returns 400 | Expected: the `firstName` pattern allows only letters, so a digit is rejected. This is the contract (pattern in `openapi.yml`), not a regression. A valid name returns 204. |
| `POST /vets` with `specialties: []` returns `specialties: []` | Expected for that input. The earlier note that specialties were dropped is not reproduced: an empty list was sent in the check. The behaviour with a non-empty list remains covered by `VetRestControllerV1Tests`. |
| `GET /petclinic/` returns 404 (original redirected to Swagger UI) | Not part of `openapi.yml`; see `migration-report.md`. |
| `POST /visits` with `petId` | Works (201, `Location: /api/visits/<id>`). |

## 4. Per-operation result of the runtime check

`in spec` means the observed status code is among the codes declared for that operation in `openapi.yml`.

| Method | Path | Observed | Declared | In spec |
|--------|------|----------|----------|---------|
| POST | /owners | 201 (and 400 on invalid) | 201, 400, 500 | yes |
| GET | /owners | 200 | 200, 304, 500 | yes |
| GET | /owners/{ownerId} | 200 (404 unknown) | 200, 304, 400, 404, 500 | yes |
| PUT | /owners/{ownerId} | 204 | 200, 400, 404, 500 | no (3.2) |
| DELETE | /owners/{ownerId} | 204 (404 repeated) | 200, 304, 400, 404, 500 | no (3.2) |
| POST | /owners/{ownerId}/pets | 201 | 201, 400, 404, 500 | yes |
| GET | /owners/{ownerId}/pets/{petId} | 200 | 200, 304, 400, 404, 500 | yes |
| PUT | /owners/{ownerId}/pets/{petId} | 204 | 204, 400, 404, 500 | yes |
| POST | /owners/{ownerId}/pets/{petId}/visits | 201 | 201, 400, 404, 500 | yes |
| GET | /pets | 200 | 200, 304, 500 | yes |
| GET | /pets/{petId} | 200 | 200, 304, 400, 404, 500 | yes |
| PUT | /pets/{petId} | 204 | 200, 304, 400, 404, 500 | no (3.2) |
| DELETE | /pets/{petId} | 204 | 200, 304, 400, 404, 500 | no (3.2) |
| GET | /visits | 200 | 200, 304, 500 | yes |
| GET | /visits/{visitId} | 200 | 200, 304, 400, 404, 500 | yes |
| POST | /visits | 201 | 200, 304, 400, 404, 500 | no (3.2), checked by hand |
| PUT | /visits/{visitId} | 204 | 200, 304, 400, 404, 500 | no (3.2) |
| DELETE | /visits/{visitId} | 204 | 200, 304, 400, 404, 500 | no (3.2) |
| GET | /v2/owners | 200 | 200, 304, 400, 500 | yes |
| GET | /v2/pets | 200 | 200, 400, 500 | yes |
| GET, POST, PUT, DELETE | /pettypes, /pettypes/{petTypeId} | 200, 201, 204, 204 | 200, 304, 400, 404, 500 | GET yes; POST, PUT, DELETE no (3.2) |
| GET, POST, PUT, DELETE | /specialties, /specialties/{specialtyId} | 200, 201, 204, 204 | 200, 304, 400, 404, 500 | GET yes; POST, PUT, DELETE no (3.2) |
| GET, POST, PUT, DELETE | /vets, /vets/{vetId} | 200, 201, 204 (400 with an invalid name), 204 | 200, 304, 400, 404, 500 | GET yes; POST, PUT, DELETE no (3.2) |
| POST | /users | 201 | 200, 304, 400, 404, 500 | no (3.2) |
| GET | /oops | 404 | 200, 304, 400 | no (3.1) |

## 5. Recommended actions

| # | Action | Type | Effort |
|---|--------|------|--------|
| 1 | Decide how to publish the contract: serve `openapi.yml` as is, or enable `useSwaggerAnnotations` (3.4) | Documentation / SDK generation | Low |
| 2 | Align `openapi.yml` with the real status codes (204 for PUT/DELETE, 201 for POST) | Spec fix (pre-existing) | Low |
| 3 | Implement `GET /oops` if the endpoint is wanted (3.1) | Restore documented behaviour | Low |
| 4 | Pin the validation error `title` if some client depends on it (3.3) | Compatibility | Low |
| 5 | Run the Postman collection in `src/test/postman` against the Quarkus app (`postman-tests.sh`) as a regression check on a real client | Verification | Low |

## 6. Limits of this comparison

- The Spring build was not executed, so "before" is derived from `openapi.yml`, the original controllers and the original tests, not measured.
- Only the success path and one invalid-input case per resource were exercised, with a single sample payload each. Error bodies (`schemaValidationErrors`,
  `detail`) were compared in the unit tests (`*RestControllerV1Tests`) rather than here.
- Security was disabled (the default), so `401`/`403` behaviour is not part of the comparison (see `todo-inventory.md`, section 1).
