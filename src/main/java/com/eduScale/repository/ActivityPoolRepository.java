package com.eduScale.repository;

import com.eduScale.domain.ActivityPool;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActivityPoolRepository extends MongoRepository<ActivityPool, String> {

    List<ActivityPool> findByObjectiveId(String objectiveId);
}

