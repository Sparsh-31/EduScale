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
@Document(collection = "user_objective_progress")
public class UserObjectiveProgress {

    @Id
    private String id;

    private String userId;

    private String objectiveId;

    private int completedActivities;

    private int totalActivities;

    private double completionPercentage;

    private double masteryScore;
}

