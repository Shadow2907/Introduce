package com.shino.vnpt.features.introduce.dto.response;

import com.shino.vnpt.features.image.document.ImageDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllResponse {
    private String id;
    private String title;
    private String description;
    private String url;
    private String status;
    private List<ImageDocument> imageInfo;
}
