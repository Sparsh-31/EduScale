package com.eduScale.controller;

import com.eduScale.domain.UserActivityProgress;
import com.eduScale.domain.UserObjectiveProgress;
import com.eduScale.security.ParentAuthSupport;
import com.eduScale.service.ProgressService;
import com.eduScale.service.ProgressService.ActivityResultPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final ParentAuthSupport parentAuthSupport;

    @PostMapping("/activity")
    public ResponseEntity<?> recordActivity(
            Authentication authentication,
            @RequestBody ActivityResultPayload payload) {
        if (!parentAuthSupport.isParent(authentication)
                || !parentAuthSupport.parentOwnsChild(authentication.getName(), payload.userId())) {
            return parentAuthSupport.forbidden();
        }
        UserActivityProgress progress = progressService.recordActivityResult(payload);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/objective/{userId}/{objectiveId}/recompute")
    public ResponseEntity<?> recomputeObjective(
            Authentication authentication,
            @PathVariable String userId,
            @PathVariable String objectiveId) {
        if (!parentAuthSupport.isParent(authentication)
                || !parentAuthSupport.parentOwnsChild(authentication.getName(), userId)) {
            return parentAuthSupport.forbidden();
        }
        UserObjectiveProgress progress = progressService.recomputeObjectiveProgress(userId, objectiveId);
        return ResponseEntity.ok(progress);
    }
}
