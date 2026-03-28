package com.eduScale.controller;

import com.eduScale.domain.Activity;
import com.eduScale.repository.ActivityRepository;
import com.eduScale.security.ParentAuthSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityRepository activityRepository;
    private final ParentAuthSupport parentAuthSupport;

    @GetMapping
    public ResponseEntity<?> findByIds(
            Authentication authentication,
            @RequestParam("ids") List<String> ids) {
        if (!parentAuthSupport.isParent(authentication)) {
            return parentAuthSupport.forbidden();
        }
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Set<String> idSet = Set.copyOf(ids);
        List<Activity> fetched = activityRepository.findAllById(idSet);
        Map<String, Activity> byId = new HashMap<>();
        for (Activity activity : fetched) {
            byId.put(activity.getId(), activity);
        }

        // Preserve requested order so frontend sequence rendering is stable.
        List<Activity> ordered = new ArrayList<>();
        for (String id : ids) {
            Activity activity = byId.get(id);
            if (activity != null) {
                ordered.add(activity);
            }
        }

        return ResponseEntity.ok(ordered);
    }

    @GetMapping("/by-objective/{objectiveId}")
    public ResponseEntity<?> findByObjectiveId(
            Authentication authentication,
            @PathVariable String objectiveId) {
        if (!parentAuthSupport.isParent(authentication)) {
            return parentAuthSupport.forbidden();
        }
        List<Activity> activities = activityRepository.findByObjectiveIdsContains(objectiveId);
        return ResponseEntity.ok(activities);
    }
}

