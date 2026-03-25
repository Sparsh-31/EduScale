package com.eduScale.repository;

import com.eduScale.domain.UserObjectiveProgress;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserObjectiveProgressRepository extends MongoRepository<UserObjectiveProgress, String> {

    List<UserObjectiveProgress> findByUserId(String userId);

    List<UserObjectiveProgress> findByUserIdAndObjectiveId(String userId, String objectiveId);
}

