package org.hartford.swasthraksha.controller;

import org.hartford.swasthraksha.dto.DocumentResponse;
import org.hartford.swasthraksha.model.DocumentType;
import org.hartford.swasthraksha.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PreAuthorize("hasRole('APPLICANT')")
    @PostMapping("/upload/application")
    public ResponseEntity<DocumentResponse> uploadApplicationDocument(
            @RequestParam("applicationNumber") String applicationNumber,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return ResponseEntity.ok(documentService.uploadForApplication(
                applicationNumber,
                documentType,
                file,
                authentication.getName()
        ));
    }

    @PreAuthorize("hasRole('APPLICANT')")
    @PostMapping("/upload/claim")
    public ResponseEntity<DocumentResponse> uploadClaimDocument(
            @RequestParam("claimNumber") String claimNumber,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return ResponseEntity.ok(documentService.uploadForClaim(
                claimNumber,
                documentType,
                file,
                authentication.getName()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            Authentication authentication
    ) {
        DocumentService.DownloadedDocument downloaded = documentService.getDocumentForDownload(id, authentication);
        String fileType = downloaded.metadata().getFileType() != null
                ? downloaded.metadata().getFileType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(fileType);
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloaded.metadata().getFileName() + "\"")
                .body(downloaded.resource());
    }

        @GetMapping("/application/{applicationId}")
        public ResponseEntity<List<DocumentResponse>> getApplicationDocuments(
                        @PathVariable Long applicationId,
                        Authentication authentication
        ) {
                return ResponseEntity.ok(documentService.getApplicationDocuments(applicationId, authentication));
        }

        @GetMapping("/claim/{claimId}")
        public ResponseEntity<List<DocumentResponse>> getClaimDocuments(
                        @PathVariable Long claimId,
                        Authentication authentication
        ) {
                return ResponseEntity.ok(documentService.getClaimDocuments(claimId, authentication));
        }
}
