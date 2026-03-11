package org.hartford.swasthraksha.model;



public enum ApplicationStatus {

    DRAFT,                     // Application created
    SUBMITTED,                 // Submitted by user
    UNDER_REVIEW,              // Underwriter reviewing
    APPROVED,                  // Risk approved
    REJECTED,                  // Risk rejected

    QUOTE_GENERATED,           // Premium calculated
    WAITING_CUSTOMER_ACCEPTANCE, // Waiting for user decision
    CUSTOMER_ACCEPTED,         // User accepted premium
    CUSTOMER_DECLINED,         // User rejected premium

    POLICY_ISSUED              // Final policy created
}