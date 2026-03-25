package com.eduScale.controller;

import com.eduScale.domain.Session;
import com.eduScale.repository.SessionRepository;
import com.eduScale.service.SessionGeneratorService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/start")
    public ResponseEntity<Session> startSession(
            @RequestParam String userId,
            @RequestParam String objectiveId
    ) {
        Session session = sessionGeneratorService.startSession(userId, objectiveId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/start-custom")
    public ResponseEntity<Session> startCustomSession(
            @RequestParam String userId,
            @RequestParam String objectiveId,
            @RequestBody(required = false) StartCustomSessionRequest request
    ) {
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
    public ResponseEntity<Session> getSession(@PathVariable String sessionId) {
        Optional<Session> session = sessionRepository.findById(sessionId);
        return session.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record StartCustomSessionRequest(List<String> activityIds) {}
}

