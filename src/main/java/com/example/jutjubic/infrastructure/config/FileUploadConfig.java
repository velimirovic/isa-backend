package com.example.jutjubic.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
@Getter
public class FileUploadConfig implements WebMvcConfigurer {
    @Value("${file.upload.directory:uploads}")
    private String uploadDirectory;

    @Value("${file.video.max-size:209715200}") // 200MB
    private long maxVideoSize;

    @Value("${file.image.max-size:10485760}") // 10MB
    private long maxImageSize;

    @PostConstruct
    public void init() {
        File uploadDir = new File(uploadDirectory);
        if (!uploadDir.exists()) {
            if (uploadDir.mkdirs())
                System.out.println("Upload direktorijum kreiran: " + uploadDir.getAbsolutePath());
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDirectory + "/");
    }
}
