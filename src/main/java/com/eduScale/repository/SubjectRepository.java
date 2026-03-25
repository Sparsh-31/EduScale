package com.eduScale.repository;

import com.eduScale.domain.Subject;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubjectRepository extends MongoRepository<Subject, String> {

    List<Subject> findByGradeId(String gradeId);
}
