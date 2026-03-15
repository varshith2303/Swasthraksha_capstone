# Plan: UserControllerTest

## Goal
Write complete unit tests for `UserController` following the JUnit Spring Boot guide,
mirroring the exact same structure and style as `PolicyControllerTest.java`.

---

## Guide Methods Applied

| Guide Step | Applied As |
|---|---|
| Step 5 — Mockito | `@MockBean` for `AuthService`, `UserRepository` — no real DB, no Spring context |
| Step 6 — MockMvc | `@WebMvcTest(UserController.class)` + `MockMvc` for all HTTP calls |
| Step 7 — Exceptions | `thenThrow(RuntimeException)` / `doThrow()` → assert `400` + error message body |
| Step 8 — Edge Cases | Empty list returns, duplicate email, unauthenticated access, user not found |
| Step 10 — Naming | `methodName_whenCondition_shouldExpectedResult` on every single test method |
| Step 11 — Maintainability | Named constants for URLs, emails, IDs — zero magic strings or numbers |

---

## Class Setup

```java
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtFilter.class, JwtUtil.class})
class UserControllerTest {
```

### Constants Block (Step 11 — avoid magic numbers/strings)
```
REGISTER_URL        = "/register"
ADMIN_USERS_URL     = "/admin/users"
ADMIN_USERS_BY_ID   = "/admin/users/{id}"
ADMIN_CLAIMS_URL    = "/admin/claims-officers"
ADMIN_EMAIL         = "admin@test.com"
APPLICANT_EMAIL     = "applicant@test.com"
EXISTING_ID         = 1L
NON_EXISTENT_ID     = 999L
```

### MockBeans
- `AuthService authService` — mocks register, registerUnderwriter, registerClaimsOfficer, deleteUser
- `UserRepository userRepository` — mocks findByRoleContaining
- `UserDetailsService userDetailsService` — required by JwtFilter / SecurityConfig

### Fixtures in `@BeforeEach`
Build three user objects using named fields (not inline literals):
- `applicantUser`    — id=1, username="John Doe",    email="john@example.com",  role="APPLICANT"
- `underwriterUser`  — id=2, username="Alice Writer", email="alice@example.com", role="UNDERWRITER"
- `claimsOfficerUser`— id=3, username="Bob Claims",  email="bob@example.com",   role="CLAIMS_OFFICER"

---

## Tests — 20 Total

### POST /register  (2 tests)
1. `register_whenValidUserSubmitsRegistration_shouldReturn201WithApplicantRole`
   - `when(authService.register(any())).thenReturn(applicantUser)`
   - Assert: `201`, `$.email`, `$.role == "APPLICANT"`
   - `verify(authService, times(1)).register(any())`

2. `register_whenEmailAlreadyExists_shouldReturn400WithMessage`
   - `thenThrow(RuntimeException("This email is already registered: john@example.com"))`
   - Assert: `400`, `$.message` contains email
   - Step 7 — exception testing

---

### POST /admin/users  (4 tests)
3. `registerUnderwriter_whenAdminSubmitsValidUser_shouldReturn201WithUnderwriterRole`
   - `when(authService.registerUnderwriter(any())).thenReturn(underwriterUser)`
   - Assert: `201`, `$.role == "UNDERWRITER"`
   - `verify(authService, times(1)).registerUnderwriter(any())`

4. `registerUnderwriter_whenNonAdminSubmits_shouldReturn403`
   - `.with(user(APPLICANT_EMAIL).roles("APPLICANT"))`
   - Assert: `403`
   - `verifyNoInteractions(authService)`

5. `registerUnderwriter_whenRequestIsUnauthenticated_shouldReturn401`
   - No `.with(user(...))` — unauthenticated
   - Assert: `401`
   - `verifyNoInteractions(authService)`

6. `registerUnderwriter_whenEmailAlreadyExists_shouldReturn400WithMessage`
   - `thenThrow(RuntimeException("This email is already registered: alice@example.com"))`
   - Assert: `400`, `$.message` contains email

---

### GET /admin/users  (4 tests)
7. `getUnderwriters_whenAdminRequests_shouldReturn200WithUnderwriterList`
   - `when(userRepository.findByRoleContaining("UNDERWRITER")).thenReturn(List.of(underwriterUser))`
   - Assert: `200`, `$` has size 1, `$[0].role == "UNDERWRITER"`
   - `verify(userRepository, times(1)).findByRoleContaining("UNDERWRITER")`

8. `getUnderwriters_whenNoUnderwritersExist_shouldReturn200WithEmptyList`
   - `thenReturn(List.of())`
   - Assert: `200`, `$` has size 0
   - Step 8 — edge case: empty list

9. `getUnderwriters_whenNonAdminRequests_shouldReturn403`
   - Assert: `403`
   - `verifyNoInteractions(userRepository)`

10. `getUnderwriters_whenRequestIsUnauthenticated_shouldReturn401`
    - Assert: `401`
    - `verifyNoInteractions(userRepository)`

---

### POST /admin/claims-officers  (3 tests)
11. `registerClaimsOfficer_whenAdminSubmitsValidUser_shouldReturn201WithClaimsOfficerRole`
    - `when(authService.registerClaimsOfficer(any())).thenReturn(claimsOfficerUser)`
    - Assert: `201`, `$.role == "CLAIMS_OFFICER"`
    - `verify(authService, times(1)).registerClaimsOfficer(any())`

12. `registerClaimsOfficer_whenNonAdminSubmits_shouldReturn403`
    - Assert: `403`
    - `verifyNoInteractions(authService)`

13. `registerClaimsOfficer_whenEmailAlreadyExists_shouldReturn400WithMessage`
    - `thenThrow(RuntimeException("This email is already registered: bob@example.com"))`
    - Assert: `400`, `$.message` contains email

---

### GET /admin/claims-officers  (3 tests)
14. `getClaimsOfficers_whenAdminRequests_shouldReturn200WithClaimsOfficerList`
    - `when(userRepository.findByRoleContaining("CLAIMS_OFFICER")).thenReturn(List.of(claimsOfficerUser))`
    - Assert: `200`, `$` has size 1, `$[0].role == "CLAIMS_OFFICER"`
    - `verify(userRepository, times(1)).findByRoleContaining("CLAIMS_OFFICER")`

15. `getClaimsOfficers_whenNoClaimsOfficersExist_shouldReturn200WithEmptyList`
    - `thenReturn(List.of())`
    - Assert: `200`, `$` has size 0
    - Step 8 — edge case: empty list

16. `getClaimsOfficers_whenNonAdminRequests_shouldReturn403`
    - Assert: `403`
    - `verifyNoInteractions(userRepository)`

---

### DELETE /admin/users/{id}  (4 tests)
17. `deleteUser_whenAdminDeletesExistingUser_shouldReturn200WithSuccessMessage`
    - `doNothing().when(authService).deleteUser(EXISTING_ID)`
    - Assert: `200`, body == `"User deleted successfully"`
    - `verify(authService, times(1)).deleteUser(EXISTING_ID)`

18. `deleteUser_whenNonAdminRequestsDelete_shouldReturn403`
    - Assert: `403`
    - `verifyNoInteractions(authService)`

19. `deleteUser_whenRequestIsUnauthenticated_shouldReturn401`
    - Assert: `401`
    - `verifyNoInteractions(authService)`

20. `deleteUser_whenUserDoesNotExist_shouldReturn400WithMessage`
    - `doThrow(new IllegalArgumentException("User not found with id: 999")).when(authService).deleteUser(NON_EXISTENT_ID)`
    - Assert: `400`, `$.message` contains `"999"`
    - Step 7 — exception testing

---

## Summary

| Endpoint | Tests |
|---|---|
| POST /register | 2 |
| POST /admin/users | 4 |
| GET /admin/users | 4 |
| POST /admin/claims-officers | 3 |
| GET /admin/claims-officers | 3 |
| DELETE /admin/users/{id} | 4 |
| **Total** | **20** |

