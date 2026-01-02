package com.example.jutjubic.core.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

public interface FileStoringService {
    String storeFile(MultipartFile file, Optional<String> draftId);
    boolean isFileExtensionValid(MultipartFile file, String fileType);
    boolean deleteFile(String fileName);
}
