package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.core.service.FileStoringService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Service
public class FileStoringServiceImpl implements FileStoringService {
    public String storeFile(MultipartFile file, Optional<String> draftId) {
        if (!isFileValid(file))
            throw new RuntimeException("An error occured: file not valid");

        Path uploadDir = Paths.get(System.getProperty("user.dir"),"uploads");
        String fileName = draftId.orElse("") + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);

        try {
            if (!Files.exists(uploadDir))
                Files.createDirectories(uploadDir);

            try (FileOutputStream fout = new FileOutputStream(filePath.toFile())) {
                fout.write(file.getBytes());
            }

            return fileName;
        } catch (Exception e) {
            deleteFile(filePath.toString());
            throw new RuntimeException("Error in uploading file: " + e);
        }
    }

    private boolean isFileValid(MultipartFile file) {
        return file.getSize() <= 209715200 && !file.isEmpty();
    }

    public boolean isFileExtensionValid(MultipartFile file, String fileType) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return false;

        String extension = fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase();

        if (fileType.equals("video")) {
            return Set.of("mp4", "mov", "wmv", "avi", "mkv", "webm")
                    .contains(extension);
        } else if (fileType.equals("image")) {
            return Set.of("jpg", "jpeg", "png", "webp")
                    .contains(extension);
        } else {
            return false;
        }
    }

    public boolean deleteFile(String fileName) {
        try {
            return Files.deleteIfExists(Paths.get(System.getProperty("user.dir"), "uploads").resolve(fileName));
        } catch (Exception e) {
            throw new RuntimeException("File deleting error: " + e);
        }
    }

    public Resource loadFile(String fileName) {
        try {
            Path uploadDir = Paths.get(System.getProperty("user.dir"),"uploads");
            Path filePath = uploadDir.resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (MalformedURLException  e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }
}
