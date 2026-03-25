package com.eduScale.domain;

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
@Document(collection = "activities")
public class Activity {

    @Id
    private String id;

    /**
     * Short display title for the activity, used in the UI.
     */
    private String title;

    /**
     * Child-friendly instruction text shown above the interaction area.
     */
    private String instruction;

    @Indexed
    private EngineType engineType;

    @Indexed
    private ActivityType activityType;

    @Indexed
    private DifficultyLevel difficultyLevel;

    /**
     * Learning objectives this activity supports.
     */
    @Indexed
    private List<String> objectiveIds;

    /**
     * Estimated time in seconds.
     */
    private int estimatedTimeSeconds;

    /**
     * Arbitrary tags to help selection and analytics.
     */
    private List<String> tags;

    /**
     * Engine-specific configuration payload.
     */
    private Object configJson; // [App] Need to fix this
}

