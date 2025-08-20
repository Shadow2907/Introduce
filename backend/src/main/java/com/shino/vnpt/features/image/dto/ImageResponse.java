package com.shino.vnpt.features.image.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponse {
    private String id;
    private String title;
    private String description;
    private String url;
}
