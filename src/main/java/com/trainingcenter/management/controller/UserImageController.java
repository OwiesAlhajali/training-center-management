package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.UserResponseDTO;
import com.trainingcenter.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserImageController {

    private final UserService userService;

    @PostMapping(value = "/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDTO uploadUserImage(@PathVariable("id") Long id,
                                          @RequestParam("image") MultipartFile image) {
        return userService.uploadUserImage(id, image);
    }
}
