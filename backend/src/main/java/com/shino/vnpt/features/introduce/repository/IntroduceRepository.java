package com.shino.vnpt.features.introduce.repository;

import com.shino.vnpt.features.introduce.document.IntroduceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntroduceRepository extends MongoRepository<IntroduceDocument, String> {
    boolean existsByTitle(String title);
    IntroduceDocument findByTitle(String title);
}
