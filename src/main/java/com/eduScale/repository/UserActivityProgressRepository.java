package com.eduScale.repository;

import com.eduScale.domain.UserActivityProgress;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityProgressRepository extends MongoRepository<UserActivityProgress, String> {

    List<UserActivityProgress> findByUserId(String userId);

    List<UserActivityProgress> findByUserIdAndActivityId(String userId, String activityId);
}

