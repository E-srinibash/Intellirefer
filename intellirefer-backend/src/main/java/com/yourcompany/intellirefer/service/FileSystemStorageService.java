package com.yourcompany.intellirefer.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileSystemStorageService {

    @Value("${storage.location}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(storageLocation);
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String store(MultipartFile file, String subfolder) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (file.isEmpty() || originalFilename.contains("..")) {
            throw new RuntimeException("Failed to store empty file or file with invalid path sequence " + originalFilename);
        }

        try {
            // Generate a unique filename to prevent overwrites
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

            // Resolve the path against the root location
            Path destinationFolder = this.rootLocation.resolve(subfolder);
            Files.createDirectories(destinationFolder); // Ensure subfolder exists
            Path destinationFile = destinationFolder.resolve(uniqueFilename);

            // Copy the file to the target location
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return the relative path to be stored in the database
            return Paths.get(subfolder, uniqueFilename).toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    public Resource loadAsResource(String relativePath) {
        try {
            Path file = rootLocation.resolve(relativePath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + relativePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + relativePath, e);
        }
    }

    public void delete(String relativePath) {
        try {
            if (relativePath != null) {
                Path file = rootLocation.resolve(relativePath);
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            // Log this error but don't throw, as failing to delete shouldn't break the app
            System.err.println("Failed to delete file: " + relativePath);
        }
    }
}