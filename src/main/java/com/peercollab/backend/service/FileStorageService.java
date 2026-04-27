package com.peercollab.backend.service;

import com.peercollab.backend.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/zip",
            "application/x-zip-compressed",
            "application/octet-stream"
    );

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private Path rootPath;

    @PostConstruct
    void init() throws IOException {
        rootPath = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(rootPath);
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please upload a file.");
        }
        String originalName = file.getOriginalFilename() == null ? "attachment" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String fileNameLower = originalName.toLowerCase(Locale.ROOT);
        boolean validExtension = fileNameLower.endsWith(".pdf") || fileNameLower.endsWith(".zip");
        if (!validExtension) {
            throw new BadRequestException("Only PDF and ZIP files are allowed.");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!ALLOWED_CONTENT_TYPES.contains(contentType) && !"application/x-zip-compressed".equalsIgnoreCase(contentType))) {
            throw new BadRequestException("Unsupported file content type.");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("File size must be 10 MB or less.");
        }

        String storedName = UUID.randomUUID() + "-" + originalName.replace(" ", "_");
        Path target = rootPath.resolve(storedName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BadRequestException("Unable to store the uploaded file.");
        }

        return new StoredFile(originalName, storedName, contentType, file.getSize(), target);
    }

    public Resource loadAsResource(String storedFileName) {
        Path path = rootPath.resolve(storedFileName).normalize();
        if (!path.startsWith(rootPath)) {
            throw new BadRequestException("Invalid file path.");
        }
        return new FileSystemResource(path);
    }

    public record StoredFile(
            String originalFileName,
            String storedFileName,
            String contentType,
            long size,
            Path path
    ) {
    }
}
