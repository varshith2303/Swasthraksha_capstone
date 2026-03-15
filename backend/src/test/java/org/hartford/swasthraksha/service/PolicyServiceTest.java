package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.model.Policy;
import org.hartford.swasthraksha.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PolicyService.
 *
 * Layer   : Service (no Spring context)
 * Tool    : @ExtendWith(MockitoExtension.class) + @Mock + @InjectMocks
 * Naming  : methodName_whenCondition_shouldExpectedResult
 */
@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock private PolicyRepository policyRepository;

    @InjectMocks private PolicyService policyService;

    private Policy policy;

    @BeforeEach
    void setUp() {
        policy = new Policy();
        policy.setId(1L);
        policy.setPolicyName("Health Basic");
        policy.setPolicyCode("HEAL_001");
        policy.setMinCoverage(50_000.0);
        policy.setMaxCoverage(500_000.0);
        policy.setBasePercent(2.5);
        policy.setActive(true);
        policy.setPlanType("BOTH");
    }

    // ══════════════════════════════════════════════════════════════════════
    // addPolicy
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addPolicy - when policy code is unique - should save and return the new policy")
    void addPolicy_whenUniqueCode_shouldSaveAndReturnPolicy() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(null);
        when(policyRepository.save(policy)).thenReturn(policy);

        Policy result = policyService.addPolicy(policy);

        assertThat(result.getPolicyCode()).isEqualTo("HEAL_001");
        assertThat(result.getPolicyName()).isEqualTo("Health Basic");
        verify(policyRepository).save(policy);
    }

    @Test
    @DisplayName("addPolicy - when policy code already exists - should throw RuntimeException without saving")
    void addPolicy_whenDuplicateCode_shouldThrowRuntimeExceptionWithoutSaving() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> policyService.addPolicy(policy));

        assertThat(ex.getMessage()).contains("already exists");
        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("addPolicy - when policy code is null - should attempt save (no duplicates check on null)")
    void addPolicy_whenNullPolicyCode_shouldAttemptSaveWithoutDuplicateCheck() {
        Policy nullCodePolicy = new Policy();
        nullCodePolicy.setPolicyCode(null);

        when(policyRepository.getByPolicyCode(null)).thenReturn(null);
        when(policyRepository.save(nullCodePolicy)).thenReturn(nullCodePolicy);

        assertDoesNotThrow(() -> policyService.addPolicy(nullCodePolicy));
        verify(policyRepository).save(nullCodePolicy);
    }

    // ══════════════════════════════════════════════════════════════════════
    // deletePolicy
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deletePolicy - when policy exists - should delete and return without exception")
    void deletePolicy_whenPolicyExists_shouldDeleteSuccessfully() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        assertDoesNotThrow(() -> policyService.deletePolicy(1L));

        verify(policyRepository).delete(policy);
    }

    @Test
    @DisplayName("deletePolicy - when policy not found - should throw RuntimeException")
    void deletePolicy_whenPolicyNotFound_shouldThrowRuntimeException() {
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> policyService.deletePolicy(999L));

        assertThat(ex.getMessage()).contains("not found");
        verify(policyRepository, never()).delete(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // toggleStatus
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("toggleStatus - when policy is currently active - should set inactive and return updated policy")
    void toggleStatus_whenPolicyIsActive_shouldDeactivatePolicy() {
        policy.setActive(true);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(policy)).thenReturn(policy);

        Policy result = policyService.toggleStatus(1L);

        assertThat(result.isActive()).isFalse();
        verify(policyRepository).save(policy);
    }

    @Test
    @DisplayName("toggleStatus - when policy is currently inactive - should set active and return updated policy")
    void toggleStatus_whenPolicyIsInactive_shouldActivatePolicy() {
        policy.setActive(false);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(policy)).thenReturn(policy);

        Policy result = policyService.toggleStatus(1L);

        assertThat(result.isActive()).isTrue();
        verify(policyRepository).save(policy);
    }

    @Test
    @DisplayName("toggleStatus - when policy not found - should throw RuntimeException")
    void toggleStatus_whenPolicyNotFound_shouldThrowRuntimeException() {
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> policyService.toggleStatus(999L));

        verify(policyRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // getAllPolicies
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllPolicies - when policies exist - should return complete list")
    void getAllPolicies_whenPoliciesExist_shouldReturnCompleteList() {
        Policy second = new Policy();
        second.setId(2L);
        second.setPolicyCode("HEAL_002");

        when(policyRepository.findAll()).thenReturn(List.of(policy, second));

        List<Policy> result = policyService.getAllPolicies();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Policy::getPolicyCode)
                .containsExactly("HEAL_001", "HEAL_002");
    }

    @Test
    @DisplayName("getAllPolicies - when no policies exist - should return empty list")
    void getAllPolicies_whenNoPoliciesExist_shouldReturnEmptyList() {
        when(policyRepository.findAll()).thenReturn(List.of());

        List<Policy> result = policyService.getAllPolicies();

        assertThat(result).isEmpty();
    }

    // ══════════════════════════════════════════════════════════════════════
    // updatePolicy
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updatePolicy - when valid update with its own existing code - should save and return updated policy")
    void updatePolicy_whenCodeBelongsToSamePolicy_shouldSaveAndReturn() {
        Policy update = new Policy();
        update.setPolicyCode("HEAL_001");
        update.setPolicyName("Updated Health Basic");

        // findById returns existing; getByPolicyCode returns the same policy (self)
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);
        when(policyRepository.save(update)).thenReturn(update);

        Policy result = policyService.updatePolicy(1L, update);

        assertThat(update.getId()).isEqualTo(1L);
        verify(policyRepository).save(update);
    }

    @Test
    @DisplayName("updatePolicy - when policy not found - should throw RuntimeException")
    void updatePolicy_whenPolicyNotFound_shouldThrowRuntimeException() {
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> policyService.updatePolicy(999L, new Policy()));

        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePolicy - when new code belongs to a different policy - should throw RuntimeException")
    void updatePolicy_whenCodeBelongsToAnotherPolicy_shouldThrowRuntimeException() {
        Policy conflicting = new Policy();
        conflicting.setId(2L);           // different ID than the one being updated (id=1)
        conflicting.setPolicyCode("HEAL_001");

        Policy update = new Policy();
        update.setPolicyCode("HEAL_001");

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(conflicting);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> policyService.updatePolicy(1L, update));

        assertThat(ex.getMessage()).contains("already exists");
        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePolicy - when new code does not exist elsewhere - should save with updated id")
    void updatePolicy_whenNewCodeIsUnique_shouldSaveWithCorrectId() {
        Policy update = new Policy();
        update.setPolicyCode("HEAL_UPDATED");
        update.setPolicyName("Updated Policy");

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.getByPolicyCode("HEAL_UPDATED")).thenReturn(null);
        when(policyRepository.save(update)).thenReturn(update);

        policyService.updatePolicy(1L, update);

        assertThat(update.getId()).isEqualTo(1L);
        verify(policyRepository).save(update);
    }
}
