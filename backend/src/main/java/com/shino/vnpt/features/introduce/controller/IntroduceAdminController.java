package com.shino.vnpt.features.introduce.controller;

import com.shino.vnpt.features.introduce.dto.request.IntroduceCreateRequest;
import com.shino.vnpt.features.introduce.dto.request.IntroduceUpdateRequest;
import com.shino.vnpt.features.introduce.enums.Status;
import com.shino.vnpt.features.introduce.service.IntroduceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/admin/introduce")
public class IntroduceAdminController {
    private final IntroduceService introduceService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createIntroduce(@ModelAttribute IntroduceCreateRequest introduceCreateRequest, @RequestPart("file") MultipartFile multipartFile) {
        try {
            return ResponseEntity.ok(introduceService.createIntroduce(introduceCreateRequest, multipartFile));
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateIntroduce(@RequestPart String id, @ModelAttribute IntroduceUpdateRequest introduceUpdateRequest, @RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        try {
            return ResponseEntity.ok(introduceService.updateIntroduce(id, introduceUpdateRequest, multipartFile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/update-status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam Status status) {
        try {
            return ResponseEntity.ok(introduceService.updateStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteIntroduce(@PathVariable String id) {
        try {
            return ResponseEntity.ok(introduceService.deleteIntroduce(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
