package com.eduScale.domain;

import java.time.Instant;
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
@Document(collection = "user_activity_history")
public class UserActivityHistory {

    @Id
    private String id;

    private String userId;

    private String activityId;

    private String sessionId;

    private Instant timestamp;
}

