# Software Requirements Specification (SRS) for Swastharaksha

## 1. Introduction
### 1.1 Purpose
This Software Requirements Specification (SRS) outlines the functional and non-functional requirements for the **Swastharaksha** health insurance application.

### 1.2 Scope
Swastharaksha is an end-to-end web-based health insurance portal that facilitates policy viewing, application submissions, application processing, policy assignment, and claim processing. The application features a robust role-based access control system to securely isolate user workflows across Applicants, Administrators, Underwriters, and Claims Officers.

## 2. Overall Description
### 2.1 User Classes and Characteristics
The system strictly supports four main types of actors (roles), each with distinct dashboards and functionality:
*   **Applicant / User (`APPLICANT`)**: End-users who apply for insurance policies, accept/decline underwritten quotes, and submit medical claims.
*   **Administrator (`admin`)**: Super-users managing the core policy definitions and delegating cross-system tasks (assigning applications and claims to workflow officers).
*   **Underwriter (`UNDERWRITER`)**: Risk-assessment officers responsible for reviewing insurance applications, determining final premiums, and updating statuses.
*   **Claims Officer (`CLAIMS_OFFICER`)**: Assessors resolving and verifying submitted medical insurance claims.

## 3. System Features

### 3.1 Authentication & Authorization Module
*   **User Registration & Login**: Users can register and subsequently authenticate into the application.
*   **Role-Based Access Control & Routing**: The frontend actively utilizes Route Guards (`authGuard`, `adminGuard`, `underwriterGuard`, `claimsOfficerGuard`, `guestGuard`) to secure dashboards based on user context.

### 3.2 Public / Landing Portal
*   **Policy Catalog**: Public users can view active insurance policies. The catalog allows filtering between various plan types: `INDIVIDUAL`, `FAMILY`, or `BOTH`.

### 3.3 Applicant / User Workflow
*   **Application Management**:
    *   Submit a new health insurance application (providing details, which can include adding household policy members).
    *   View all historically submitted insurance applications.
    *   Accept or Decline finalized insurance offers proposed by the Underwriter.
*   **Claim Management**:
    *   Submit a new compensation claim against an active policy.
    *   View personal claim history and current claim statuses.

### 3.4 Administrator Workflow
*   **Policy Management**:
    *   Create new insurance policies.
    *   Update existing policy details.
    *   Soft-delete policies by toggling their "active" status.
    *   Permanently delete policies.
    *   View the full catalog of policies (both active and inactive).
*   **Workflow Delegation**:
    *   View all submitted system applications.
    *   Assign pending insurance applications to specific Underwriters.
    *   View all submitted system claims.
    *   Assign pending claims to specific Claims Officers.

### 3.5 Underwriter Workflow
*   **Application Assessment**:
    *   View pending (unassigned) applications.
    *   View specifically assigned applications.
    *   Retrieve all member details associated with a given application.
    *   Make an underwriting decision by updating an application's status and providing the `Final Premium`.

### 3.6 Claims Officer Workflow
*   **Claim Verification**:
    *   View claims assigned securely to their queue.
    *   Verify (approve or reject) assigned claims.

## 4. System Models and Data Entities
The application structures its core data across the following models:
*   **User & Identity**: `Users`, [Role](file:///d:/health_insurance/backend/src/main/java/org/hartford/swasthraksha/model/Role.java#8-15)
*   **Policy Definitions**: [Policy](file:///d:/health_insurance/backend/src/main/java/org/hartford/swasthraksha/controller/PolicyController.java#21-27), `PolicyStatus`
*   **Application & Underwriting**: [Application](file:///d:/health_insurance/backend/src/main/java/org/hartford/swasthraksha/controller/ApplicationController.java#33-39), [ApplicationStatus](file:///d:/health_insurance/backend/src/main/java/org/hartford/swasthraksha/controller/ApplicationController.java#68-77), `PolicyMember`, `Relationship`, `PolicyAssignment`
*   **Claim Operations**: [Claim](file:///d:/health_insurance/backend/src/main/java/org/hartford/swasthraksha/controller/ClaimController.java#31-36), `ClaimStatus`
*   **Supplementary**: `Document`, `DocumentType` (used for document attachments and references).

## 5. Technology Stack
*   **Backend**: Java/Spring Boot framework featuring modular service-controller architecture ([ApplicationController](file:///d:/health_insurance/backend/src/main/java/org/hartford/swasthraksha/controller/ApplicationController.java#17-103), [ClaimController](file:///d:/health_insurance/backend/src/main/java/org/hartford/swasthraksha/controller/ClaimController.java#16-67), [PolicyController](file:///d:/health_insurance/backend/src/main/java/org/hartford/swasthraksha/controller/PolicyController.java#14-73), `AuthController`, etc.) backed by JPA/Hibernate Entities. Spring Security is implemented with method-level authorization (e.g., `@PreAuthorize`).
*   **Frontend**: Angular framework utilizing components organized by features (`landing`, `user-dashboard`, `auth`, `admin`, `underwriter`, `claims-officer`) and routing architecture.
