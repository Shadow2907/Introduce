package com.shino.vnpt.features.introduce.service;

import com.shino.vnpt.features.image.document.ImageDocument;
import com.shino.vnpt.features.image.services.ImageService;
import com.shino.vnpt.features.introduce.document.IntroduceDocument;
import com.shino.vnpt.features.introduce.dto.request.IntroduceCreateRequest;
import com.shino.vnpt.features.introduce.dto.request.IntroduceUpdateRequest;
import com.shino.vnpt.features.introduce.enums.Status;
import com.shino.vnpt.features.introduce.mapper.IntroduceMapper;
import com.shino.vnpt.features.introduce.repository.IntroduceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Comparator;
import java.util.List;
@Service
@RequiredArgsConstructor
public class IntroduceServiceImpl implements  IntroduceService {
    private final IntroduceMapper introduceMapper;
    private final IntroduceRepository introduceRepository;
    private final ImageService imageService;

    @Override
    public IntroduceDocument createIntroduce(IntroduceCreateRequest introduceCreateRequest, MultipartFile multipartFile) {
        try {

            ImageDocument imageDocument = imageService.saveImage(multipartFile);
            IntroduceDocument introduceDocument = introduceMapper.toIntroduceDocument(introduceCreateRequest);
            introduceDocument.setTitle(introduceCreateRequest.getTitle().toUpperCase());
            introduceDocument.setImageId(imageDocument.getId());
            introduceRepository.save(introduceDocument);
            return introduceDocument;
        } catch (Exception e) {
            throw new RuntimeException("Error in introduce create" + e.getMessage() + e);
        }
    }



    @Override
    public List<IntroduceDocument> getAllIntroduce() {
        return introduceRepository.findAll()
                .stream()
                .sorted(Comparator
                        .comparing((IntroduceDocument doc) -> !"Production".equalsIgnoreCase(doc.getTag())) // false < true
                        .thenComparing(IntroduceDocument::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }


    @Override
    public IntroduceDocument getById(String id) {
        try {
            return introduceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Introduce not found with ID: " + id));
        } catch (Exception e) {
            throw new RuntimeException("Error in getById" + e.getMessage() + e);
        }
    }

    @Override
    public IntroduceDocument updateIntroduce(String id, IntroduceUpdateRequest introduceUpdateRequest, MultipartFile multipartFile) {
        try {
            IntroduceDocument introduceDocument = getById(id);

//            if (introduceUpdateRequest.getTitle() != null) {
//                if (introduceRepository.existsByTitle(introduceUpdateRequest.getTitle().toUpperCase())) {
//                    throw new RuntimeException("TITLE ALREADY EXISTS");
//                } else {
//                    introduceDocument.setTitle(introduceUpdateRequest.getTitle().toUpperCase());
//                }
//            }

            introduceDocument.setTitle(introduceUpdateRequest.getTitle().toUpperCase());

            if (multipartFile != null) {
                ImageDocument imageDocument = imageService.saveImage(multipartFile);
                introduceDocument.setImageId(imageDocument.getId());
            }

            introduceMapper.updateIntroduceDocument(introduceUpdateRequest, introduceDocument);


            introduceRepository.save(introduceDocument);
            return introduceDocument;
        } catch (Exception e) {
            throw new RuntimeException("Error in updateIntroduce" + e.getMessage() + e);
        }
    }

    @Override
    public IntroduceDocument updateStatus(String id, Status status) {
        try {
            IntroduceDocument introduceDocument = getById(id);
            introduceDocument.setStatus(status);
            introduceRepository.save(introduceDocument);
            return introduceDocument;
        } catch (Exception e) {
            throw new RuntimeException("Error in updateStatus" + e.getMessage() + e);
        }
    }

    @Override
    public IntroduceDocument deleteIntroduce(String id) {
        try {
            IntroduceDocument introduceDocument = getById(id);

            introduceRepository.delete(introduceDocument);
            return introduceDocument;
        } catch (Exception e) {
            throw new RuntimeException("Error in deleteIntroduce" + e.getMessage() + e);
        }
    }
}
