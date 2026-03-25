package com.eduScale.domain;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sessions")
public class Session {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String objectiveId;

    private List<String> activityIds;

    private boolean completed;

    private Instant startedAt;

    private Instant completedAt;
}

