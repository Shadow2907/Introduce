package com.shino.vnpt.features.introduce.controller;

import com.shino.vnpt.features.introduce.document.IntroduceDocument;
import com.shino.vnpt.features.introduce.service.IntroduceService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IntroduceGraphQlController {
    private final IntroduceService introduceService;

    @QueryMapping
    public List<IntroduceDocument> introduces() {
        return introduceService.getAllIntroduce();
    }
}
