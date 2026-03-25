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
@Document(collection = "activity_pools")
public class ActivityPool {

    @Id
    private String id;

    @Indexed
    private String objectiveId;

    private String name;

    /**
     * Activities belonging to this pool.
     */
    private List<String> activityIds;
}

