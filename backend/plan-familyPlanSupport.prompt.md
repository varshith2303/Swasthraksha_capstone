# Plan: Family Plan Support with Member-Based Underwriting

## Overview

Add family plan support so that applications with a single member auto-process via the existing underwriting logic, while applications with multiple family members are routed to `UNDER_REVIEW` for manual underwriter review. All `PolicyMember` fields are validated when multiple members are present.

---

## Steps

### 1. Add Getters/Setters to `PolicyMember.java`
Expose all fields (`name`, `age`, `bmi`, `smoker`, `existingDiseases`) with proper accessors so the service can read member data.

**File:** `src/main/java/org/hartford/swasthraksha/model/PolicyMember.java`

---

### 2. Create a `PolicyMemberRequest` DTO
Create a new DTO in the `dto/` package mirroring all `PolicyMember` fields (`name`, `age`, `bmi`, `smoker`, `existingDiseases`) to receive member data from the client.

**File:** `src/main/java/org/hartford/swasthraksha/dto/PolicyMemberRequest.java`

---

### 3. Update `ApplicationRequest.java`
Add a `List<PolicyMemberRequest> members` field with getter/setter. Keep the existing top-level fields (`age`, `bmi`, `smoker`, `existingDiseases`) as backward-compatible fallback for individual plans when no `members` list is provided.

**File:** `src/main/java/org/hartford/swasthraksha/dto/ApplicationRequest.java`

---

### 4. Update `ApplicationService.apply()`
Branch on the size of the `members` list:

- **No members list, or 1 member:**
  - Use the single member's fields (or top-level fields if no list) to run `calculateRiskIndex` and `determineStatus` exactly as today.
  - Auto-approve/reject based on risk score.
  - If a single `PolicyMember` is provided in the list, persist it linked to the application.

- **2+ members:**
  - Validate that every `PolicyMemberRequest` has all required fields non-null (`name`, `age`, `bmi`, `smoker`, `existingDiseases`). Throw a descriptive `RuntimeException` (or use Bean Validation) if any field is missing.
  - Persist all `PolicyMember` entities linked to the application.
  - Set application status directly to `UNDER_REVIEW` (skip automated underwriting decision).
  - Calculate `proposedPremium` as the **sum of each member's individual risk-based premium** using `calculateRiskIndex` + `calculateFinalPremium` per member, so the underwriter has a reference figure to override.
  - Set `riskScore` to the average risk across all members.
  - Do **not** set `WAITING_CUSTOMER_ACCEPTANCE` — the underwriter must act first.

**File:** `src/main/java/org/hartford/swasthraksha/service/ApplicationService.java`

---

### 5. Ensure `assignToUnderwriter()` Handles Pre-Set `UNDER_REVIEW` Applications
Family-plan apps arrive already in `UNDER_REVIEW`. The existing guard already allows `UNDER_REVIEW` as a valid state for assignment, so verify no regression — the status must **not** be reset when it is already `UNDER_REVIEW`.

**File:** `src/main/java/org/hartford/swasthraksha/service/ApplicationService.java`

---

### 6. Update `ApplicationController.java` (Optional Endpoint)
No endpoint signature changes are required for submission. Optionally, add a `GET /applications/{id}/members` endpoint (accessible by `UNDERWRITER` and `admin`) to retrieve the full list of `PolicyMember` records for a given application during the review process.

**File:** `src/main/java/org/hartford/swasthraksha/controller/ApplicationController.java`

---

## Further Considerations

1. **Primary applicant field strategy** — The top-level `age`/`bmi`/`smoker`/`existingDiseases` fields on `ApplicationRequest` are kept as an individual-plan shorthand (backward-compatible). When `members` is provided and non-empty, they take precedence and the top-level fields are ignored. This avoids breaking existing clients.

2. **Family premium calculation** — For multi-member plans, `proposedPremium` is computed as the sum of each member's risk-based premium (using `calculateRiskIndex` + `calculateFinalPremium` per member). The underwriter can override the final premium when making their decision via the existing `updateApplicationStatus()` endpoint.

3. **Validation layer** — Member field validation for multi-member plans uses manual null checks in the service layer (throwing `RuntimeException` with a clear message). Jakarta Bean Validation (`@NotNull` + `@Valid`) can be layered on top in a future pass for cleaner error responses.

4. **`Application.members` field** — The `Application` entity already has `@OneToMany(mappedBy = "application", cascade = CascadeType.ALL) List<PolicyMember> members`. No model change is needed; the service only needs to populate and link the list before saving.

5. **`PolicyMember.application` back-reference** — When persisting members, each `PolicyMember`'s `application` field must be set to the saved `Application` instance so the FK `application_id` is populated correctly.

