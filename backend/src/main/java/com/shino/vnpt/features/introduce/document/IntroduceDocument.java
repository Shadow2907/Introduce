package com.shino.vnpt.features.introduce.document;

import com.shino.vnpt.features.introduce.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "introduce")
public class IntroduceDocument {
    @Id
    private String id;
    private String title;
    private String description;
    private String tag;
    private String imageId;
    private String url;
    @CreatedDate
    private LocalDateTime createAt;
    @LastModifiedDate
    private LocalDateTime modifiedAt;
    private Status status = Status.ACTIVE;
}
