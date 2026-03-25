package com.eduScale.repository;

import com.eduScale.domain.Chapter;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChapterRepository extends MongoRepository<Chapter, String> {

    List<Chapter> findBySubjectId(String subjectId);

    List<Chapter> findBySubjectIdIn(List<String> subjectIds);
}
