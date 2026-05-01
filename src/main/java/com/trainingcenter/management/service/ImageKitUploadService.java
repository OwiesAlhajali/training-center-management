package com.trainingcenter.management.service;

import com.trainingcenter.management.config.ImageKitConfig.ImageKitProperties;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.ExternalServiceException;
import io.imagekit.client.ImageKitClient;
import io.imagekit.models.files.FileUploadParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ImageKitUploadService {

    private final ImageKitClient imageKitClient;
    private final ImageKitProperties imageKitProperties;

    public String uploadImage(MultipartFile imageFile) {
        validateImageFile(imageFile);

        String fileName = imageFile.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            fileName = "training-session-image";
        }

        try (InputStream inputStream = imageFile.getInputStream()) {
            FileUploadParams.Builder paramsBuilder = FileUploadParams.builder()
                    .file(inputStream)
                    .fileName(fileName);

            if (imageKitProperties.getPublicKey() != null && !imageKitProperties.getPublicKey().isBlank()) {
                paramsBuilder.publicKey(imageKitProperties.getPublicKey());
            }

            return imageKitClient.files().upload(paramsBuilder.build()).url()
                    .orElseThrow(() -> new ExternalServiceException("ImageKit upload succeeded but no URL was returned."));
        } catch (IOException ex) {
            throw new ExternalServiceException("Failed to read the uploaded image.", ex);
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ExternalServiceException("Failed to upload the image to ImageKit.", ex);
        }
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BadRequestException("Image file is required.");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed.");
        }
    }
}