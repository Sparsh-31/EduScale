package com.eduScale.domain;

import java.time.Instant;
import java.util.List;
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
@Document(collection = "content_packs")
public class ContentPack {

    @Id
    private String id;

    private String packId;

    private String name;

    private String description;

    private String version;

    private String checksum;

    private Instant updatedAt;

    /**
     * Learning objectives covered by this pack.
     */
    private List<String> objectiveIds;
}

