package com.shino.vnpt.features.introduce.dto.response;

import com.shino.vnpt.features.introduce.document.IntroduceDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntroduceResponse {
    private IntroduceDocument introduceDocument;
    private String message;
}
