package com.eduScale.controller;

import com.eduScale.domain.UserActivityProgress;
import com.eduScale.domain.UserObjectiveProgress;
import com.eduScale.service.ProgressService;
import com.eduScale.service.ProgressService.ActivityResultPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/activity")
    public ResponseEntity<UserActivityProgress> recordActivity(@RequestBody ActivityResultPayload payload) {
        UserActivityProgress progress = progressService.recordActivityResult(payload);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/objective/{userId}/{objectiveId}/recompute")
    public ResponseEntity<UserObjectiveProgress> recomputeObjective(
            @PathVariable String userId,
            @PathVariable String objectiveId
    ) {
        UserObjectiveProgress progress = progressService.recomputeObjectiveProgress(userId, objectiveId);
        return ResponseEntity.ok(progress);
    }
}

