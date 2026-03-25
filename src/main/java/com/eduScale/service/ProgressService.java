package com.eduScale.service;

import com.eduScale.domain.UserActivityProgress;
import com.eduScale.domain.UserObjectiveProgress;

public interface ProgressService {

    UserActivityProgress recordActivityResult(ActivityResultPayload payload);

    UserObjectiveProgress recomputeObjectiveProgress(String userId, String objectiveId);

    record ActivityResultPayload(
            String userId,
            String sessionId,
            String activityId,
            boolean correct,
            int attempts,
            double responseTimeSeconds,
            int hintUsageCount,
            String difficultyLevel
    ) {}
}

