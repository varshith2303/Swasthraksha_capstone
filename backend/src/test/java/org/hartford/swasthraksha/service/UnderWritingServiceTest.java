package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.model.ApplicationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Unit tests for UnderWritingService (pure calculation — no Spring context, no mocks).
 *
 * Layer   : Service (no Spring context, no mocks needed)
 * Tool    : @ExtendWith(MockitoExtension.class) + @InjectMocks
 * Naming  : methodName_whenCondition_shouldExpectedResult
 *
 * Risk formula weights:
 *   ageRisk × 0.25 + smokerRisk × 0.30 + diseaseRisk × 0.30 + bmiRisk × 0.15
 */
@ExtendWith(MockitoExtension.class)
class UnderWritingServiceTest {

    @InjectMocks
    private UnderWritingService underWritingService;

    // ══════════════════════════════════════════════════════════════════════
    // calculateRiskIndex
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("calculateRiskIndex - when young non-smoker with healthy BMI and no diseases - should return low risk (0.05)")
    void calculateRiskIndex_whenYoungHealthyNonSmoker_shouldReturnLowRisk() {
        // age<30 → ageRisk=0.2; nonSmoker → 0.0; noDisease → 0.0; bmi≤25 → 0.0
        // risk = 0.2×0.25 + 0×0.30 + 0×0.30 + 0×0.15 = 0.05
        double risk = underWritingService.calculateRiskIndex(25, 22.0, false, "");

        assertThat(risk).isCloseTo(0.05, within(0.001));
    }

    @Test
    @DisplayName("calculateRiskIndex - when elderly smoker with high BMI and diseases - should return maximum risk (1.0)")
    void calculateRiskIndex_whenElderlySmokerHighBmiWithDiseases_shouldReturnMaximumRisk() {
        // age>60 → 1.0; smoker → 1.0; diseases → 1.0; bmi>30 → 1.0
        // risk = 1.0×0.25 + 1.0×0.30 + 1.0×0.30 + 1.0×0.15 = 1.0
        double risk = underWritingService.calculateRiskIndex(65, 35.0, true, "Diabetes, Heart Disease");

        assertThat(risk).isCloseTo(1.0, within(0.001));
    }

    @Test
    @DisplayName("calculateRiskIndex - when middle-aged non-smoker with moderate BMI and no diseases - should return medium-low risk")
    void calculateRiskIndex_whenMiddleAgedModerateRiskProfile_shouldReturnMediumLowRisk() {
        // age<=45 → 0.4; nonSmoker → 0.0; noDisease → 0.0; 25<bmi≤30 → 0.5
        // risk = 0.4×0.25 + 0×0.30 + 0×0.30 + 0.5×0.15 = 0.10 + 0.075 = 0.175
        double risk = underWritingService.calculateRiskIndex(40, 28.0, false, "");

        assertThat(risk).isCloseTo(0.175, within(0.001));
    }

    @Test
    @DisplayName("calculateRiskIndex - when age is exactly 45 - should use ageRisk 0.4 boundary")
    void calculateRiskIndex_whenAgeIsExactly45_shouldUseAgeRisk04() {
        // age=45 → ageRisk=0.4 (boundary: ≤45 → 0.4)
        double risk45 = underWritingService.calculateRiskIndex(45, 22.0, false, "");
        double risk44 = underWritingService.calculateRiskIndex(44, 22.0, false, "");

        assertThat(risk45).isEqualTo(risk44);
    }

    @Test
    @DisplayName("calculateRiskIndex - when age is exactly 60 - should use ageRisk 0.7 boundary")
    void calculateRiskIndex_whenAgeIsExactly60_shouldUseAgeRisk07() {
        // age=60 → ageRisk=0.7 (boundary: ≤60 → 0.7)
        double risk60 = underWritingService.calculateRiskIndex(60, 22.0, false, "");
        double risk59 = underWritingService.calculateRiskIndex(59, 22.0, false, "");

        // Both should use ageRisk=0.7 since 59≤60 also falls in that bracket
        assertThat(risk60).isEqualTo(risk59);
    }

    @Test
    @DisplayName("calculateRiskIndex - when diseases is null - should treat as no disease risk")
    void calculateRiskIndex_whenNullDiseases_shouldTreatAsNoDiseaseRisk() {
        double riskWithNull  = underWritingService.calculateRiskIndex(25, 22.0, false, null);
        double riskWithEmpty = underWritingService.calculateRiskIndex(25, 22.0, false, "");

        assertThat(riskWithNull).isEqualTo(riskWithEmpty);
    }

    @Test
    @DisplayName("calculateRiskIndex - when smoker is null - should treat as non-smoker (0 smoker risk)")
    void calculateRiskIndex_whenNullSmoker_shouldTreatAsNonSmoker() {
        double riskNull  = underWritingService.calculateRiskIndex(25, 22.0, null, "");
        double riskFalse = underWritingService.calculateRiskIndex(25, 22.0, false, "");

        assertThat(riskNull).isEqualTo(riskFalse);
    }

    @Test
    @DisplayName("calculateRiskIndex - when BMI is exactly 25 - should have zero BMI risk")
    void calculateRiskIndex_whenBmiIsExactly25_shouldHaveZeroBmiRisk() {
        // bmi≤25 → bmiRisk=0.0
        double riskAt25 = underWritingService.calculateRiskIndex(25, 25.0, false, "");
        double riskAt24 = underWritingService.calculateRiskIndex(25, 24.0, false, "");

        assertThat(riskAt25).isEqualTo(riskAt24);
    }

    // ══════════════════════════════════════════════════════════════════════
    // determineStatus
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("determineStatus - when risk index is exactly 0.40 - should return APPROVED (inclusive boundary)")
    void determineStatus_whenRiskIsExactly040_shouldReturnApproved() {
        assertThat(underWritingService.determineStatus(0.40)).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("determineStatus - when risk index is below 0.40 - should return APPROVED")
    void determineStatus_whenRiskBelow040_shouldReturnApproved() {
        assertThat(underWritingService.determineStatus(0.05)).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(underWritingService.determineStatus(0.0)).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("determineStatus - when risk index is 0.41 - should return UNDER_REVIEW (just above APPROVED boundary)")
    void determineStatus_whenRiskIsJustAbove040_shouldReturnUnderReview() {
        assertThat(underWritingService.determineStatus(0.41)).isEqualTo(ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("determineStatus - when risk index is exactly 0.75 - should return UNDER_REVIEW (inclusive boundary)")
    void determineStatus_whenRiskIsExactly075_shouldReturnUnderReview() {
        assertThat(underWritingService.determineStatus(0.75)).isEqualTo(ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("determineStatus - when risk index is 0.76 - should return REJECTED (just above UNDER_REVIEW boundary)")
    void determineStatus_whenRiskIsJustAbove075_shouldReturnRejected() {
        assertThat(underWritingService.determineStatus(0.76)).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("determineStatus - when risk index is 1.0 (maximum) - should return REJECTED")
    void determineStatus_whenMaximumRisk_shouldReturnRejected() {
        assertThat(underWritingService.determineStatus(1.0)).isEqualTo(ApplicationStatus.REJECTED);
    }

    // ══════════════════════════════════════════════════════════════════════
    // calculateFinalPremium
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("calculateFinalPremium - when low risk and 1-year duration - should compute premium without discount")
    void calculateFinalPremium_whenLowRiskOneYearDuration_shouldComputePremiumWithoutDiscount() {
        // basePremium = 100_000 × (2.0/100) = 2000
        // multiplier = 1 + 0.2×1.5 = 1.3   (below cap)
        // premium = 2000 × 1.3 = 2600   (no duration discount: dur=1)
        double premium = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 1);

        assertThat(premium).isCloseTo(2600.0, within(0.01));
    }

    @Test
    @DisplayName("calculateFinalPremium - when risk produces multiplier above 2.5 - should cap multiplier at 2.5")
    void calculateFinalPremium_whenVeryHighRisk_shouldCapMultiplierAt2Point5() {
        // riskIndex=1.0 → multiplier = 1+1.0×1.5 = 2.5  (exactly at cap)
        // basePremium = 2000; premium = 2000×2.5 = 5000
        double premiumAtCap = underWritingService.calculateFinalPremium(100_000.0, 2.0, 1.0, 1);

        // riskIndex=2.0 → would give 4.0, capped to 2.5 → same result
        double premiumBeyondCap = underWritingService.calculateFinalPremium(100_000.0, 2.0, 2.0, 1);

        assertThat(premiumAtCap).isCloseTo(5000.0, within(0.01));
        assertThat(premiumBeyondCap).isEqualTo(premiumAtCap);
    }

    @Test
    @DisplayName("calculateFinalPremium - when duration is >= 5 years - should apply 10% discount")
    void calculateFinalPremium_whenDurationAtLeast5Years_shouldApply10PercentDiscount() {
        double premiumNoDuration = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 1);
        double premiumWith5Yrs  = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 5);
        double premiumWith10Yrs = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 10);

        assertThat(premiumWith5Yrs).isCloseTo(premiumNoDuration * 0.90, within(0.01));
        assertThat(premiumWith10Yrs).isCloseTo(premiumNoDuration * 0.90, within(0.01));
    }

    @Test
    @DisplayName("calculateFinalPremium - when duration is 3 or 4 years - should apply 5% discount")
    void calculateFinalPremium_whenDuration3Or4Years_shouldApply5PercentDiscount() {
        double premiumNoDuration = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 1);
        double premiumWith3Yrs  = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 3);
        double premiumWith4Yrs  = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 4);

        assertThat(premiumWith3Yrs).isCloseTo(premiumNoDuration * 0.95, within(0.01));
        assertThat(premiumWith4Yrs).isCloseTo(premiumNoDuration * 0.95, within(0.01));
    }

    @Test
    @DisplayName("calculateFinalPremium - when duration is 2 years - should apply no duration discount")
    void calculateFinalPremium_whenDuration2Years_shouldApplyNoDurationDiscount() {
        double premiumNoDuration = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 1);
        double premiumWith2Yrs  = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 2);

        assertThat(premiumWith2Yrs).isCloseTo(premiumNoDuration, within(0.01));
    }

    @Test
    @DisplayName("calculateFinalPremium - when duration is null - should skip duration discount gracefully")
    void calculateFinalPremium_whenNullDuration_shouldSkipDurationDiscount() {
        double premiumNoDuration = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, 1);
        double premiumNullDur   = underWritingService.calculateFinalPremium(100_000.0, 2.0, 0.2, null);

        assertThat(premiumNullDur).isCloseTo(premiumNoDuration, within(0.01));
    }

    // ══════════════════════════════════════════════════════════════════════
    // calculateFamilyPremium
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("calculateFamilyPremium - when 2 members - should apply 5% member-count discount")
    void calculateFamilyPremium_when2Members_shouldApply5PercentMemberDiscount() {
        // avgRisk = (0.2+0.3)/2 = 0.25
        // basePremium = 100_000×0.02 = 2000; multiplier = 1+0.25×1.5 = 1.375
        // premium before member discount = 2000×1.375 = 2750  (dur=1, no duration discount)
        // 2-member discount: 2750×0.95 = 2612.50
        double[] risks = {0.2, 0.3};
        double premium = underWritingService.calculateFamilyPremium(100_000.0, 2.0, risks, 1);

        assertThat(premium).isCloseTo(2612.5, within(0.01));
    }

    @Test
    @DisplayName("calculateFamilyPremium - when 3 members - should apply 10% member-count discount")
    void calculateFamilyPremium_when3Members_shouldApply10PercentMemberDiscount() {
        // 3-member discount (10%) should yield lower premium than equivalent 2-member rate
        double[] risks2 = {0.2, 0.3};
        double[] risks3 = {0.2, 0.3, 0.25}; // similar avg risk

        double premium2 = underWritingService.calculateFamilyPremium(100_000.0, 2.0, risks2, 1);
        double premium3 = underWritingService.calculateFamilyPremium(100_000.0, 2.0, risks3, 1);

        // 3-member 10% discount > 2-member 5% discount, so premium3 should be lower
        assertThat(premium3).isLessThan(premium2);
    }

    @Test
    @DisplayName("calculateFamilyPremium - when 4 or more members - should apply 15% member-count discount")
    void calculateFamilyPremium_whenFourOrMoreMembers_shouldApply15PercentMemberDiscount() {
        // Same avg risk as 2-member case, but 4-member discount is 15% vs 5%
        double[] risks2 = {0.25, 0.25};
        double[] risks4 = {0.25, 0.25, 0.25, 0.25};

        double premium2 = underWritingService.calculateFamilyPremium(100_000.0, 2.0, risks2, 1);
        double premium4 = underWritingService.calculateFamilyPremium(100_000.0, 2.0, risks4, 1);

        assertThat(premium4).isLessThan(premium2);
    }

    @Test
    @DisplayName("calculateFamilyPremium - when 5 years duration with 3 members - should apply both duration and member discounts")
    void calculateFamilyPremium_when5YearDurationAnd3Members_shouldApplyBothDiscounts() {
        double[] risks = {0.2, 0.3, 0.1};

        double premiumShortTerm = underWritingService.calculateFamilyPremium(100_000.0, 2.0, risks, 1);
        double premiumLongTerm  = underWritingService.calculateFamilyPremium(100_000.0, 2.0, risks, 5);

        // 5-year plan gets both 10% duration AND 10% 3-member discount → should be cheaper
        assertThat(premiumLongTerm).isLessThan(premiumShortTerm);
    }

    @Test
    @DisplayName("calculateFamilyPremium - when single member risks array - should compute like individual premium with 0% member discount")
    void calculateFamilyPremium_whenSingleMember_shouldComputeWithNoMemberDiscount() {
        double[] risks = {0.2};
        // basePremium=2000; multiplier=1+0.2×1.5=1.3; premium=2600; no member discount (size=1 hits no if-branch)
        double premium = underWritingService.calculateFamilyPremium(100_000.0, 2.0, risks, 1);

        assertThat(premium).isCloseTo(2600.0, within(0.01));
    }
}
