package com.eduScale.controller;

import com.eduScale.domain.Session;
import com.eduScale.repository.SessionRepository;
import com.eduScale.security.ParentAuthSupport;
import com.eduScale.service.SessionGeneratorService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionGeneratorService sessionGeneratorService;
    private final SessionRepository sessionRepository;
    private final ParentAuthSupport parentAuthSupport;

    @PostMapping("/start")
    public ResponseEntity<?> startSession(
            Authentication authentication,
            @RequestParam String userId,
            @RequestParam String objectiveId) {
        if (!parentAuthSupport.isParent(authentication)
                || !parentAuthSupport.parentOwnsChild(authentication.getName(), userId)) {
            return parentAuthSupport.forbidden();
        }
        Session session = sessionGeneratorService.startSession(userId, objectiveId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/start-custom")
    public ResponseEntity<?> startCustomSession(
            Authentication authentication,
            @RequestParam String userId,
            @RequestParam String objectiveId,
            @RequestBody(required = false) StartCustomSessionRequest request) {
        if (!parentAuthSupport.isParent(authentication)
                || !parentAuthSupport.parentOwnsChild(authentication.getName(), userId)) {
            return parentAuthSupport.forbidden();
        }
        List<String> activityIds = request != null && request.activityIds() != null
                ? request.activityIds()
                : List.of();

        Session session = Session.builder()
                .userId(userId)
                .objectiveId(objectiveId)
                .activityIds(activityIds)
                .completed(false)
                .startedAt(Instant.now())
                .build();

        return ResponseEntity.ok(sessionRepository.save(session));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSession(Authentication authentication, @PathVariable String sessionId) {
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Session session = sessionOpt.get();
        if (!parentAuthSupport.isParent(authentication)
                || !parentAuthSupport.parentOwnsChild(authentication.getName(), session.getUserId())) {
            return parentAuthSupport.forbidden();
        }
        return ResponseEntity.ok(session);
    }

    public record StartCustomSessionRequest(List<String> activityIds) {}
}
