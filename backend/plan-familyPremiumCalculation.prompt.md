# Plan: Family Premium Calculation Rework

## Problem

The current family plan premium logic calls `calculateFinalPremium()` **once per member** and sums the results. This is incorrect for real-world family health insurance where:

- The `requestedCoverage` is a **shared pool** — all members draw from the same coverage amount, not individual pots.
- Premium should be calculated **once** on that shared coverage, risk-weighted by the family's collective profile.
- Families should be rewarded with a **multi-member discount** for adding more members.

---

## Proposed Logic

### Step 1 — Per-Member Risk Index
Calculate each member's risk score individually using the existing `calculateRiskIndex()`.

### Step 2 — Average (Family) Risk
```
avgRisk = sum(memberRisks) / memberCount
```
This represents the collective risk profile of the family unit.

### Step 3 — Single Premium on Shared Coverage
```
basePremium = coverage × (basePercent / 100)
multiplier  = 1 + (avgRisk × 1.5)   [capped at 2.5]
premium     = basePremium × multiplier
```
Only **one** premium is calculated — not one per member.

### Step 4 — Duration Discount (same as individual)
```
duration >= 5 years → 10% off
duration >= 3 years →  5% off
```

### Step 5 — Multi-Member Discount (applied after duration discount)
```
2 members  →  5% off
3 members  → 10% off
4+ members → 15% off
```

---

## Changes Required

### 1. `UnderWritingService.java` — Add `calculateFamilyPremium()`

Add a new method alongside the existing `calculateFinalPremium()`:

```java
public double calculateFamilyPremium(Double coverage,
        Double baseRatePercent,
        double[] memberRisks,
        Integer duration) {

    // Average risk
    double totalRisk = 0.0;
    for (double r : memberRisks) totalRisk += r;
    double avgRisk = totalRisk / memberRisks.length;

    // Single premium on shared coverage
    double basePremium = coverage * (baseRatePercent / 100);
    double multiplier = 1 + (avgRisk * 1.5);
    if (multiplier > MAX_MULTIPLIER) multiplier = MAX_MULTIPLIER;
    double premium = basePremium * multiplier;

    // Duration discount
    if (duration != null) {
        if (duration >= 5)      premium *= 0.90;
        else if (duration >= 3) premium *= 0.95;
    }

    // Multi-member discount
    int memberCount = memberRisks.length;
    if (memberCount >= 4)      premium *= 0.85;
    else if (memberCount == 3) premium *= 0.90;
    else if (memberCount == 2) premium *= 0.95;

    return premium;
}
```

---

### 2. `ApplicationService.java` — Replace family premium block

Replace the current block that calls `calculateFinalPremium()` per member and sums `totalPremium`:

**Before (incorrect):**
```java
double totalRisk = 0.0;
double totalPremium = 0.0;
for (PolicyMemberRequest m : memberRequests) {
    double memberRisk = underWritingService.calculateRiskIndex(...);
    double memberPremium = underWritingService.calculateFinalPremium(
            coverageAmount, p.getBasePercent(), memberRisk, a.getDuration());
    totalRisk += memberRisk;
    totalPremium += memberPremium;
}
double avgRisk = totalRisk / memberRequests.size();
app.setRiskScore(avgRisk);
app.setProposedPremium(totalPremium);
```

**After (correct):**
```java
// Calculate individual risk per member
double[] memberRisks = new double[memberRequests.size()];
for (int i = 0; i < memberRequests.size(); i++) {
    PolicyMemberRequest m = memberRequests.get(i);
    memberRisks[i] = underWritingService.calculateRiskIndex(
            m.getAge(), m.getBmi(), m.getSmoker(), m.getExistingDiseases());
}

// Single shared premium using average risk + member-count discount
double familyPremium = underWritingService.calculateFamilyPremium(
        coverageAmount, p.getBasePercent(), memberRisks, a.getDuration());

double avgRisk = 0.0;
for (double r : memberRisks) avgRisk += r;
avgRisk = avgRisk / memberRisks.length;

app.setRiskScore(avgRisk);
app.setProposedPremium(familyPremium);
```

---

## Example Walkthrough (from user's request)

**Input:** coverage = ₹15,00,000, basePercent = 5%, duration = 2 years, 2 members

| Member  | Age | BMI | Smoker | Diseases | Risk                                          |
|---------|-----|-----|--------|----------|-----------------------------------------------|
| Sanjay  | 30  | 40  | true   | ""       | (0.4×0.25)+(1.0×0.30)+(0.0×0.30)+(1.0×0.15) = **0.55** |
| Prakash | 50  | 45  | true   | "bp"     | (0.7×0.25)+(1.0×0.30)+(1.0×0.30)+(1.0×0.15) = **0.925** |

```
avgRisk      = (0.55 + 0.925) / 2 = 0.7375
basePremium  = 15,00,000 × 5% = ₹75,000
multiplier   = 1 + (0.7375 × 1.5) = 2.10625
premium      = ₹75,000 × 2.10625 = ₹1,57,968.75
duration=2   → no discount
2 members    → 5% off → ₹1,57,968.75 × 0.95 = ₹1,50,069.31

proposedPremium = ₹1,50,069.31  (underwriter reviews and can override)
```

**Compare to old (wrong) approach:** ₹1,36,875 + ₹1,79,062 = ₹3,15,937 — nearly double, because it treated coverage as per-member, not shared.

---

## Further Considerations

1. **No change to coverage validation** — `requestedCoverage` is still validated flat against `minCoverage`/`maxCoverage`. This is correct since it is the shared pool amount.
2. **Status stays `UNDER_REVIEW`** — family plans always go to the underwriter regardless of the computed avg risk, since the underwriter may adjust `finalPremium`.
3. **`riskScore` on Application** — stores the average family risk, giving the underwriter a single reference figure.
4. **Backward compatibility** — individual plan flow (`calculateFinalPremium`) is completely untouched.

