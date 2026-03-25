package com.eduScale.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_activity_progress")
public class UserActivityProgress {

    @Id
    private String id;

    private String userId;

    private String activityId;

    private int attemptCount;

    private int correctCount;

    private double accuracy;

    private double avgResponseTimeSeconds;

    private int hintUsageCount;

    private DifficultyLevel difficultyLevel;

    private boolean completed;
}

