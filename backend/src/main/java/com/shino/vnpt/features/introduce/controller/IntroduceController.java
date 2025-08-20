package com.shino.vnpt.features.introduce.controller;


import com.shino.vnpt.features.introduce.service.IntroduceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/introduce")
public class IntroduceController {
    private final IntroduceService introduceService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllIntroduce() {
        try {
            return ResponseEntity.ok(introduceService.getAllIntroduce());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-by-title/{id}")
    public ResponseEntity<?> getIntroduceByTitle(@PathVariable String id) {
        try {
            return ResponseEntity.ok(introduceService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
