package com.eduScale.repository;

import com.eduScale.domain.Session;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepository extends MongoRepository<Session, String> {

    List<Session> findByUserId(String userId);

    List<Session> findByUserIdIn(List<String> userIds);

    List<Session> findByUserIdAndObjectiveId(String userId, String objectiveId);
}

