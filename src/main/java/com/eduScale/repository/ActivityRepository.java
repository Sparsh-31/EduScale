package com.eduScale.repository;

import com.eduScale.domain.Activity;
import com.eduScale.domain.ActivityType;
import com.eduScale.domain.EngineType;
import com.eduScale.domain.DifficultyLevel;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActivityRepository extends MongoRepository<Activity, String> {

    List<Activity> findByObjectiveIdsContains(String objectiveId);

    List<Activity> findByActivityType(ActivityType activityType);

    List<Activity> findByEngineType(EngineType engineType);

    List<Activity> findByActivityTypeAndEngineType(ActivityType activityType, EngineType engineType);

    List<Activity> findByActivityTypeAndDifficultyLevel(ActivityType activityType, DifficultyLevel difficultyLevel);
}

