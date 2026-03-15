package org.hartford.swasthraksha.repository;

import org.hartford.swasthraksha.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
	List<Document> findByApplication_IdOrderByUploadedDateDesc(Long applicationId);

	List<Document> findByClaim_IdOrderByUploadedDateDesc(Long claimId);
}
