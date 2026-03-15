# ER Diagram Prompt — Swasthraksha Health Insurance System

Generate a complete ER diagram for the Swasthraksha Health Insurance backend system based on the following entity descriptions and relationships.

---

## Entities and Their Attributes

### 1. Users
| Column | Type | Constraint |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| username | VARCHAR | NOT NULL |
| email | VARCHAR | UNIQUE, NOT NULL |
| password | VARCHAR | |
| role | VARCHAR | e.g. APPLICANT, UNDERWRITER, CLAIMS_OFFICER, admin |

---

### 2. Policy
| Column | Type | Constraint |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| policyName | VARCHAR | |
| policyCode | VARCHAR | UNIQUE, NOT NULL |
| minCoverage | DOUBLE | |
| maxCoverage | DOUBLE | |
| basePercent | DOUBLE | |
| active | BOOLEAN | |
| planType | VARCHAR | Values: INDIVIDUAL, FAMILY, BOTH |

---

### 3. Application
| Column | Type | Constraint |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| applicationNumber | VARCHAR | |
| status | ENUM(ApplicationStatus) | DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, QUOTE_GENERATED, WAITING_CUSTOMER_ACCEPTANCE, CUSTOMER_ACCEPTED, CUSTOMER_DECLINED, POLICY_ISSUED |
| requestedCoverage | DOUBLE | |
| duration | INTEGER | |
| riskScore | DOUBLE | |
| proposedPremium | DOUBLE | |
| finalPremium | DOUBLE | |
| planType | VARCHAR | INDIVIDUAL or FAMILY |
| user_id | BIGINT | FK → Users(id), NOT NULL |
| policy_id | BIGINT | FK → Policy(id), NOT NULL |
| decided_by | BIGINT | FK → Users(id), nullable |
| assigned_to | BIGINT | FK → Users(id), nullable |

---

### 4. PolicyMember
| Column | Type | Constraint |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| name | VARCHAR | |
| age | INTEGER | |
| bmi | DOUBLE | |
| smoker | BOOLEAN | |
| existingDiseases | VARCHAR | |
| relationship | ENUM(Relationship) | SELF, SPOUSE, CHILD, PARENT — NOT NULL |
| application_id | BIGINT | FK → Application(id) |

---

### 5. PolicyAssignment
| Column | Type | Constraint |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| policyNumber | VARCHAR | |
| coverageAmount | DOUBLE | |
| remainingCoverage | DOUBLE | |
| premiumAmount | DOUBLE | yearly premium |
| totalPremiumAmount | DOUBLE | full duration premium |
| premiumPaid | DOUBLE | |
| startDate | DATE | |
| endDate | DATE | |
| durationYears | INTEGER | |
| totalClaimedAmount | DOUBLE | |
| claimCount | INTEGER | |
| paymentFrequency | VARCHAR | ANNUAL / QUARTERLY |
| totalInstallments | INTEGER | |
| paidInstallments | INTEGER | |
| renewable | BOOLEAN | |
| lastRenewalDate | DATE | |
| noClaimBonusPercentage | DOUBLE | |
| status | ENUM(PolicyStatus) | PENDING_PAYMENT, ACTIVE, EXPIRED, CANCELLED, CLAIMED |
| user_id | BIGINT | FK → Users(id), NOT NULL |
| application_id | BIGINT | FK → Application(id), NOT NULL — UNIQUE (OneToOne) |

---

### 6. Claim
| Column | Type | Constraint |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| claimNumber | VARCHAR | |
| claimAmount | DOUBLE | |
| approvedAmount | DOUBLE | |
| claimReason | VARCHAR | |
| hospitalName | VARCHAR | |
| admissionDate | DATE | |
| dischargeDate | DATE | |
| status | ENUM(ClaimStatus) | PENDING, APPROVED, REJECTED |
| reviewDate | DATE | |
| rejectionReason | VARCHAR | |
| createdAt | DATETIME | |
| policy_assignment_id | BIGINT | FK → PolicyAssignment(id) |
| claimant_id | BIGINT | FK → Users(id) |
| reviewed_by | BIGINT | FK → Users(id), nullable |
| member_id | BIGINT | FK → PolicyMember(id), nullable |

---

### 7. ClaimDocument
| Column | Type | Constraint |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| documentType | VARCHAR | |
| fileUrl | VARCHAR | |
| uploadedAt | DATETIME | |
| claim_id | BIGINT | FK → Claim(id) |

---

## Relationships

| From | Relationship | To | FK Column | Notes |
|---|---|---|---|---|
| Application | Many-to-One | Users | user_id | The applicant who submitted |
| Application | Many-to-One | Policy | policy_id | The policy being applied for |
| Application | Many-to-One | Users | decided_by | Underwriter who approved/rejected |
| Application | Many-to-One | Users | assigned_to | Underwriter assigned for review |
| Application | One-to-Many | PolicyMember | application_id | Family members on the application |
| PolicyMember | Many-to-One | Application | application_id | Each member belongs to one application |
| PolicyAssignment | Many-to-One | Users | user_id | Policy holder |
| PolicyAssignment | One-to-One | Application | application_id | One application leads to one policy assignment |
| Claim | Many-to-One | PolicyAssignment | policy_assignment_id | Claim made against an active policy |
| Claim | Many-to-One | Users | claimant_id | User raising the claim |
| Claim | Many-to-One | Users | reviewed_by | Claims officer who reviewed |
| Claim | Many-to-One | PolicyMember | member_id | Which member the claim is for |
| ClaimDocument | Many-to-One | Claim | claim_id | Documents attached to a claim |

---

## Enums (not separate tables, stored as VARCHAR/STRING in columns)

| Enum | Values |
|---|---|
| ApplicationStatus | DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, QUOTE_GENERATED, WAITING_CUSTOMER_ACCEPTANCE, CUSTOMER_ACCEPTED, CUSTOMER_DECLINED, POLICY_ISSUED |
| PolicyStatus | PENDING_PAYMENT, ACTIVE, EXPIRED, CANCELLED, CLAIMED |
| ClaimStatus | PENDING, APPROVED, REJECTED |
| Relationship | SELF, SPOUSE, CHILD, PARENT |

---

## Entity Relationship Summary (for diagram layout)

```
Users ──────────────────────────────────────────────────────┐
  │ (applicant)          │ (decidedBy)     │ (assignedTo)   │ (claimant/reviewedBy)
  ▼                      ▼                 ▼                 ▼
Application ──────► Policy          PolicyAssignment ◄─── Claim ◄─── ClaimDocument
  │                                        ▲                  │
  │ (OneToMany)                            │ (OneToOne)        │ (patient)
  ▼                                        │                   ▼
PolicyMember ───────────────────────────────            PolicyMember
```

### Key Cardinalities
- **Users → Application** : One user can have MANY applications (1:N)
- **Policy → Application** : One policy can be used in MANY applications (1:N)
- **Application → PolicyMember** : One application can have MANY members (1:N) — for family plans
- **Application → PolicyAssignment** : One application produces exactly ONE policy assignment (1:1)
- **Users → PolicyAssignment** : One user can have MANY active policy assignments (1:N)
- **PolicyAssignment → Claim** : One policy assignment can have MANY claims (1:N)
- **Claim → ClaimDocument** : One claim can have MANY documents (1:N)
- **Users → Claim** (claimant) : One user can raise MANY claims (1:N)
- **Users → Claim** (reviewedBy) : One claims officer can review MANY claims (1:N)
- **PolicyMember → Claim** : One member can be the patient in MANY claims (1:N)

