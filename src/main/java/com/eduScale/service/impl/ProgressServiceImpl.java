package com.eduScale.service.impl;

import com.eduScale.domain.DifficultyLevel;
import com.eduScale.domain.UserActivityProgress;
import com.eduScale.domain.UserObjectiveProgress;
import com.eduScale.repository.UserActivityProgressRepository;
import com.eduScale.repository.UserObjectiveProgressRepository;
import com.eduScale.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final UserActivityProgressRepository activityProgressRepository;
    private final UserObjectiveProgressRepository objectiveProgressRepository;

    @Override
    public UserActivityProgress recordActivityResult(ActivityResultPayload payload) {
        DifficultyLevel difficulty = DifficultyLevel.valueOf(payload.difficultyLevel().toUpperCase());

        UserActivityProgress progress = activityProgressRepository
                .findByUserIdAndActivityId(payload.userId(), payload.activityId())
                .stream()
                .findFirst()
                .orElse(UserActivityProgress.builder()
                        .userId(payload.userId())
                        .activityId(payload.activityId())
                        .build());

        int totalAttempts = progress.getAttemptCount() + payload.attempts();
        int totalCorrect = progress.getCorrectCount() + (payload.correct() ? 1 : 0);

        double accuracy = totalAttempts == 0 ? 0.0 : (double) totalCorrect / totalAttempts;
        double avgResponseTime = totalAttempts == 0
                ? payload.responseTimeSeconds()
                : (progress.getAvgResponseTimeSeconds() * progress.getAttemptCount()
                + payload.responseTimeSeconds())
                / totalAttempts;

        progress.setAttemptCount(totalAttempts);
        progress.setCorrectCount(totalCorrect);
        progress.setAccuracy(accuracy);
        progress.setAvgResponseTimeSeconds(avgResponseTime);
        progress.setHintUsageCount(progress.getHintUsageCount() + payload.hintUsageCount());
        progress.setDifficultyLevel(difficulty);
        progress.setCompleted(payload.correct());

        return activityProgressRepository.save(progress);
    }

    @Override
    public UserObjectiveProgress recomputeObjectiveProgress(String userId, String objectiveId) {
        // TODO: aggregate activity progress per objective once activity→objective mapping is in place.
        UserObjectiveProgress objectiveProgress = objectiveProgressRepository
                .findByUserIdAndObjectiveId(userId, objectiveId)
                .stream()
                .findFirst()
                .orElse(UserObjectiveProgress.builder()
                        .userId(userId)
                        .objectiveId(objectiveId)
                        .build());

        // Placeholder mastery formula until detailed aggregation is wired:
        objectiveProgress.setMasteryScore(objectiveProgress.getCompletionPercentage());

        return objectiveProgressRepository.save(objectiveProgress);
    }
}

