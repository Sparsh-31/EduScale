package com.eduScale.repository;

import com.eduScale.domain.ContentPack;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContentPackRepository extends MongoRepository<ContentPack, String> {
}

