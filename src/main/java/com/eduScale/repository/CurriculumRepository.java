package com.eduScale.repository;

import com.eduScale.domain.Curriculum;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CurriculumRepository extends MongoRepository<Curriculum, String> {

    List<Curriculum> findAllByOrderById();
}
