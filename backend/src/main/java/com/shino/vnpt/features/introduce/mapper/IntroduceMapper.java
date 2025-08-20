package com.shino.vnpt.features.introduce.mapper;

import com.shino.vnpt.features.introduce.document.IntroduceDocument;
import com.shino.vnpt.features.introduce.dto.request.IntroduceCreateRequest;
import com.shino.vnpt.features.introduce.dto.request.IntroduceUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IntroduceMapper {
    IntroduceMapper introduceMapper = Mappers.getMapper(IntroduceMapper.class);

    IntroduceDocument toIntroduceDocument(IntroduceCreateRequest introduceCreateRequest);

    void updateIntroduceDocument(IntroduceUpdateRequest introduceUpdateRequest, @MappingTarget IntroduceDocument introduceDocument);
}
