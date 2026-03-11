package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.model.ApplicationStatus;
import org.springframework.stereotype.Service;

@Service
public class UnderWritingService {

    private static final double AGE_WEIGHT = 0.25;
    private static final double SMOKER_WEIGHT = 0.30;
    private static final double DISEASE_WEIGHT = 0.30;
    private static final double BMI_WEIGHT = 0.15;

    private static final double MAX_MULTIPLIER = 2.5;

    public double calculateRiskIndex(Integer age,
            Double bmi,
            Boolean smoker,
            String diseases) {

        double ageRisk = getAgeRisk(age);
        double smokerRisk = Boolean.TRUE.equals(smoker) ? 1.0 : 0.0;
        double diseaseRisk = (diseases != null && !diseases.isEmpty()) ? 1.0 : 0.0;
        double bmiRisk = getBmiRisk(bmi);

        return (ageRisk * AGE_WEIGHT)
                + (smokerRisk * SMOKER_WEIGHT)
                + (diseaseRisk * DISEASE_WEIGHT)
                + (bmiRisk * BMI_WEIGHT);
    }

    private double getAgeRisk(Integer age) {
        if (age < 30)
            return 0.2;
        if (age <= 45)
            return 0.4;
        if (age <= 60)
            return 0.7;
        return 1.0;
    }

    private double getBmiRisk(Double bmi) {
        if (bmi <= 25)
            return 0.0;
        if (bmi <= 30)
            return 0.5;
        return 1.0;
    }

    public double calculateFinalPremium(Double coverage,
            Double baseRatePercent,
            double riskIndex,
            Integer duration) {

        double basePremium = coverage * (baseRatePercent / 100);
        double multiplier = 1 + (riskIndex * 1.5);

        // Cap multiplier
        if (multiplier > MAX_MULTIPLIER) {
            multiplier = MAX_MULTIPLIER;
        }

        double premium = basePremium * multiplier;

        // ── Apply Discount based on Duration ────────────────────
        if (duration != null) {
            if (duration >= 5) {
                premium *= 0.90; // 10% discount
            } else if (duration >= 3) {
                premium *= 0.95; // 5% discount
            }
        }

        return premium;
    }

    public ApplicationStatus determineStatus(double riskIndex) {

        if (riskIndex <= 0.40) {
            return ApplicationStatus.APPROVED;
        } else if (riskIndex <= 0.75) {
            return ApplicationStatus.UNDER_REVIEW;
        } else {
            return ApplicationStatus.REJECTED;
        }
    }

    /**
     * Calculates a single shared premium for a family plan.
     *
     * Steps:
     *  1. Average risk across all members → family's collective risk profile.
     *  2. One premium on the shared coverage amount using that avg risk.
     *  3. Duration discount (same rules as individual plans).
     *  4. Multi-member discount applied on top:
     *       2 members  →  5% off
     *       3 members  → 10% off
     *       4+ members → 15% off
     *
     * @param coverage        Shared coverage pool amount (same for all members)
     * @param baseRatePercent Policy base rate %
     * @param memberRisks     Individual risk score for each member
     * @param duration        Policy duration in years
     * @return proposed family premium
     */
    public double calculateFamilyPremium(Double coverage,
            Double baseRatePercent,
            double[] memberRisks,
            Integer duration) {

        // Step 1 — average risk across all members
        double totalRisk = 0.0;
        for (double r : memberRisks) {
            totalRisk += r;
        }
        double avgRisk = totalRisk / memberRisks.length;

        // Step 2 — single premium on shared coverage using average risk
        double basePremium = coverage * (baseRatePercent / 100);
        double multiplier = 1 + (avgRisk * 1.5);
        if (multiplier > MAX_MULTIPLIER) {
            multiplier = MAX_MULTIPLIER;
        }
        double premium = basePremium * multiplier;

        // Step 3 — duration discount
        if (duration != null) {
            if (duration >= 5) {
                premium *= 0.90; // 10% off
            } else if (duration >= 3) {
                premium *= 0.95; // 5% off
            }
        }

        // Step 4 — multi-member discount
        int memberCount = memberRisks.length;
        if (memberCount >= 4) {
            premium *= 0.85; // 15% off
        } else if (memberCount == 3) {
            premium *= 0.90; // 10% off
        } else if (memberCount == 2) {
            premium *= 0.95; // 5% off
        }

        return premium;
    }
}