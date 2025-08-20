package com.shino.vnpt.features.image.controller;

import com.shino.vnpt.features.image.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/image")
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile multipartFile) {
        try {
            return ResponseEntity.ok(imageService.saveImage(multipartFile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-image-by-id/{id}")
    public ResponseEntity<?> getImageById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(imageService.getImageById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllImage() {
        try {
            return ResponseEntity.ok(imageService.getAllImage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteImageById(@RequestParam("id") String id) {
        try {
            return ResponseEntity.ok(imageService.deleteImageById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
