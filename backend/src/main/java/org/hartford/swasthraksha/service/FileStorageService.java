package org.hartford.swasthraksha.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private final Path baseUploadDir;

    public FileStorageService(@Value("${app.upload.base-dir:uploads}") String uploadBaseDir) {
        this.baseUploadDir = Paths.get(uploadBaseDir).toAbsolutePath().normalize();
    }

    public StoredFileData storeApplicationFile(MultipartFile file) {
        return store(file, "applications");
    }

    public StoredFileData storeClaimFile(MultipartFile file) {
        return store(file, "claims");
    }

    public Path resolvePath(String filePath) {
        Path resolved = Paths.get(filePath).toAbsolutePath().normalize();
        if (!resolved.startsWith(baseUploadDir)) {
            throw new IllegalArgumentException("Invalid file path requested");
        }
        return resolved;
    }

    private StoredFileData store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String safeFileName = Paths.get(originalFileName).getFileName().toString();
        String extension = extractExtension(safeFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only PDF, JPG, JPEG and PNG files are allowed");
        }

        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase(Locale.ROOT)
                : "";

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type. Allowed types: PDF, JPG, PNG");
        }

        String generatedFileName = UUID.randomUUID() + "." + extension;

        try {
            Files.createDirectories(baseUploadDir.resolve(folder));
            Path destination = baseUploadDir.resolve(folder).resolve(generatedFileName).normalize();
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFileData(
                    safeFileName,
                    destination.toString(),
                    contentType
            );
        } catch (IOException ex) {
            throw new RuntimeException("Unable to store file", ex);
        }
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx <= 0 || idx == fileName.length() - 1) {
            throw new IllegalArgumentException("File extension is required");
        }
        return fileName.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    public record StoredFileData(String originalFileName, String storedPath, String fileType) {
    }
}
