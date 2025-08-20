package com.shino.vnpt.features.introduce.service;

import com.shino.vnpt.features.introduce.document.IntroduceDocument;
import com.shino.vnpt.features.introduce.dto.request.IntroduceCreateRequest;
import com.shino.vnpt.features.introduce.dto.request.IntroduceUpdateRequest;
import com.shino.vnpt.features.introduce.enums.Status;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IntroduceService {
    IntroduceDocument createIntroduce(IntroduceCreateRequest introduceCreateRequest, MultipartFile multipartFile);
    List<IntroduceDocument> getAllIntroduce();
    IntroduceDocument getById(String id);
    IntroduceDocument updateIntroduce(String id, IntroduceUpdateRequest introduceUpdateRequest, MultipartFile  multipartFile);
    IntroduceDocument updateStatus(String id, Status status);
    IntroduceDocument deleteIntroduce(String id);
}
