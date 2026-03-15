package org.hartford.swasthraksha.repository;

import org.hartford.swasthraksha.model.Claim;
import org.hartford.swasthraksha.model.PolicyAssignment;
import org.hartford.swasthraksha.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByClaimant(Users claimant);

    Claim findByClaimNumber(String claimNumber);

    List<Claim> findByReviewedBy(Users officer);

    @Query("SELECT MAX(c.reviewDate) FROM Claim c WHERE c.policy = :policy and c.status = 'APPROVED'")
    Optional<LocalDateTime> findLatestClaimDateByPolicy(@Param("policy") PolicyAssignment policy);
}
