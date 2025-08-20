package com.shino.vnpt.features.introduce.dto.request;

import com.shino.vnpt.features.introduce.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntroduceUpdateRequest {
    private String title;
    private String description;
    private String tag;
    private String url;
    private Status status;
}
