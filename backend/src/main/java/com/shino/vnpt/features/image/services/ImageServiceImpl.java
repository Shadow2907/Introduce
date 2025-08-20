package com.shino.vnpt.features.image.services;

import com.shino.vnpt.features.image.document.ImageDocument;
import com.shino.vnpt.features.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;

    @Override
    public ImageDocument saveImage(MultipartFile multipartFile) {
        try {


            if (multipartFile == null || multipartFile.isEmpty()) {
                throw new RuntimeException("File is empty or not provided.");
            }

            ImageDocument imageDocument = new ImageDocument();
            imageDocument.setContentType(multipartFile.getContentType());
            byte[] bytes = multipartFile.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            imageDocument.setBase64(base64);
            imageRepository.save(imageDocument);
            return imageDocument;
        } catch (Exception e) {
            throw new RuntimeException("Error in saveImage: " + e.getMessage(), e);
        }
    }

    @Override
    public ImageDocument getImageById(String id) {
        try {
            return imageRepository.findById(id).orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
        } catch (Exception e) {
            throw new RuntimeException("Error in getImageById: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ImageDocument> getAllImage() {
        try  {
            return imageRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error in getAllImage: " + e.getMessage(), e);
        }
    }

    @Override
    public String deleteImageById(String id) {
        try {
            ImageDocument imageDocument = imageRepository.findById(id).orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
            imageRepository.delete(imageDocument);
            return "Deleted";
        } catch (Exception e) {
            throw new RuntimeException("Error in deleteImageById: " + e.getMessage(), e);
        }
    }
}
