package com.eduScale.repository;

import com.eduScale.domain.UserActivityHistory;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityHistoryRepository extends MongoRepository<UserActivityHistory, String> {

    List<UserActivityHistory> findTop20ByUserIdAndActivityIdOrderByTimestampDesc(String userId, String activityId);

    List<UserActivityHistory> findTop50ByUserIdAndSessionIdOrderByTimestampDesc(String userId, String sessionId);
}

