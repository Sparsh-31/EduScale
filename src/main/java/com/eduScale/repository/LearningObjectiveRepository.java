package com.eduScale.repository;

import com.eduScale.domain.LearningObjective;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LearningObjectiveRepository extends MongoRepository<LearningObjective, String> {

    List<LearningObjective> findByChapterId(String chapterId);

    List<LearningObjective> findByChapterIdIn(List<String> chapterIds);
}

