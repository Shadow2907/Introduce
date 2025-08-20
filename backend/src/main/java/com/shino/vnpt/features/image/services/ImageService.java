package com.shino.vnpt.features.image.services;

import com.shino.vnpt.features.image.document.ImageDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ImageService {
    ImageDocument saveImage(MultipartFile multipartFile);
    List<ImageDocument> getAllImage();
    ImageDocument getImageById(String id);
    String deleteImageById(String id);
}
