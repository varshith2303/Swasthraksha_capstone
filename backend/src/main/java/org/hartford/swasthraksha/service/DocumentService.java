package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.dto.DocumentResponse;
import org.hartford.swasthraksha.model.Application;
import org.hartford.swasthraksha.model.Claim;
import org.hartford.swasthraksha.model.Document;
import org.hartford.swasthraksha.model.DocumentType;
import org.hartford.swasthraksha.repository.ApplicationRepository;
import org.hartford.swasthraksha.repository.ClaimRepository;
import org.hartford.swasthraksha.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public DocumentResponse uploadForApplication(
            String applicationNumber,
            DocumentType documentType,
            MultipartFile file,
            String currentUserEmail
    ) {
        Application application = applicationRepository.getByApplicationNumber(applicationNumber);
        if (application == null) {
            throw new IllegalArgumentException("Application not found: " + applicationNumber);
        }

        if (!application.getUser().getEmail().equals(currentUserEmail)) {
            throw new IllegalStateException("Unauthorized: this application does not belong to you");
        }

        FileStorageService.StoredFileData stored = fileStorageService.storeApplicationFile(file);

        Document document = new Document();
        document.setApplication(application);
        document.setClaim(null);
        document.setDocumentType(documentType);
        document.setFileName(stored.originalFileName());
        document.setFilePath(stored.storedPath());
        document.setFileType(stored.fileType());
        document.setUploadedDate(LocalDateTime.now());

        return toResponse(documentRepository.save(document));
    }

    public DocumentResponse uploadForClaim(
            String claimNumber,
            DocumentType documentType,
            MultipartFile file,
            String currentUserEmail
    ) {
        Claim claim = claimRepository.findByClaimNumber(claimNumber);
        if (claim == null) {
            throw new IllegalArgumentException("Claim not found: " + claimNumber);
        }

        if (claim.getClaimant() == null || !claim.getClaimant().getEmail().equals(currentUserEmail)) {
            throw new IllegalStateException("Unauthorized: this claim does not belong to you");
        }

        FileStorageService.StoredFileData stored = fileStorageService.storeClaimFile(file);

        Document document = new Document();
        document.setApplication(null);
        document.setClaim(claim);
        document.setDocumentType(documentType);
        document.setFileName(stored.originalFileName());
        document.setFilePath(stored.storedPath());
        document.setFileType(stored.fileType());
        document.setUploadedDate(LocalDateTime.now());

        return toResponse(documentRepository.save(document));
    }

    public DownloadedDocument getDocumentForDownload(Long documentId, Authentication authentication) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        String currentUserEmail = authentication.getName();
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .map(a -> a.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        boolean privileged = roles.contains("ADMIN") || roles.contains("UNDERWRITER") || roles.contains("CLAIMS_OFFICER");
        boolean owner = isOwner(document, currentUserEmail);

        if (!privileged && !owner) {
            throw new IllegalStateException("Unauthorized to access this document");
        }

        Path path = fileStorageService.resolvePath(document.getFilePath());
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("File not found on server");
            }
            return new DownloadedDocument(document, resource);
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Unable to read file", ex);
        }
    }

        public List<DocumentResponse> getApplicationDocuments(Long applicationId, Authentication authentication) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        String currentUserEmail = authentication.getName();
        Set<String> roles = extractRoles(authentication);

        boolean isAdmin = roles.contains("ADMIN");
        boolean isOwner = application.getUser() != null && currentUserEmail.equals(application.getUser().getEmail());
        boolean isAssignedUnderwriter = roles.contains("UNDERWRITER")
            && application.getAssignedTo() != null
            && currentUserEmail.equals(application.getAssignedTo().getEmail());

        if (!isAdmin && !isOwner && !isAssignedUnderwriter) {
            throw new IllegalStateException("Unauthorized to access documents for this application");
        }

        return documentRepository.findByApplication_IdOrderByUploadedDateDesc(applicationId)
            .stream()
            .map(this::toResponse)
            .toList();
        }

        public List<DocumentResponse> getClaimDocuments(Long claimId, Authentication authentication) {
        Claim claim = claimRepository.findById(claimId)
            .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        String currentUserEmail = authentication.getName();
        Set<String> roles = extractRoles(authentication);

        boolean isAdmin = roles.contains("ADMIN");
        boolean isOwner = claim.getClaimant() != null && currentUserEmail.equals(claim.getClaimant().getEmail());
        boolean isAssignedClaimsOfficer = roles.contains("CLAIMS_OFFICER")
            && claim.getReviewedBy() != null
            && currentUserEmail.equals(claim.getReviewedBy().getEmail());

        if (!isAdmin && !isOwner && !isAssignedClaimsOfficer) {
            throw new IllegalStateException("Unauthorized to access documents for this claim");
        }

        return documentRepository.findByClaim_IdOrderByUploadedDateDesc(claimId)
            .stream()
            .map(this::toResponse)
            .toList();
        }

    private boolean isOwner(Document document, String userEmail) {
        if (document.getApplication() != null && document.getApplication().getUser() != null) {
            return userEmail.equals(document.getApplication().getUser().getEmail());
        }
        if (document.getClaim() != null && document.getClaim().getClaimant() != null) {
            return userEmail.equals(document.getClaim().getClaimant().getEmail());
        }
        return false;
    }

    private Set<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .map(a -> a.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private DocumentResponse toResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setFileName(document.getFileName());
        response.setFilePath(document.getFilePath());
        response.setFileType(document.getFileType());
        response.setDocumentType(document.getDocumentType());
        response.setUploadedDate(document.getUploadedDate());
        return response;
    }

    public record DownloadedDocument(Document metadata, Resource resource) {
    }
}
