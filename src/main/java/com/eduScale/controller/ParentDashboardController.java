package com.eduScale.controller;

import com.eduScale.domain.Session;
import com.eduScale.domain.User;
import com.eduScale.repository.SessionRepository;
import com.eduScale.repository.UserObjectiveProgressRepository;
import com.eduScale.repository.UserRepository;
import java.util.List;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parent")
@RequiredArgsConstructor
public class ParentDashboardController {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final UserObjectiveProgressRepository objectiveProgressRepository;

    @GetMapping("/{parentId}/dashboard")
    public ResponseEntity<ParentDashboardDto> getDashboard(@PathVariable String parentId) {
        List<User> children = userRepository.findByParentId(parentId);
        List<String> childIds = children.stream().map(User::getId).toList();

        List<Session> sessions = childIds.isEmpty()
                ? List.of()
                : sessionRepository.findByUserIdIn(childIds);

        int totalSessions = sessions.size();

        ParentDashboardDto dto = ParentDashboardDto.builder()
                .childrenCount(children.size())
                .totalSessions(totalSessions)
                .totalTimeMinutes(0) // placeholder; can be derived from activity progress
                .currentStreakDays(0) // placeholder; can be calculated from session dates
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/child/{childId}/progress")
    public ResponseEntity<ChildProgressDto> getChildProgress(@PathVariable String childId) {
        var objectives = objectiveProgressRepository.findByUserId(childId);
        double avgCompletion = objectives.stream()
                .mapToDouble(o -> o.getCompletionPercentage() != 0 ? o.getCompletionPercentage() : 0)
                .average()
                .orElse(0.0);

        ChildProgressDto dto = ChildProgressDto.builder()
                .childId(childId)
                .overallCompletion(avgCompletion)
                .totalObjectives(objectives.size())
                .build();

        return ResponseEntity.ok(dto);
    }

    @Builder
    private record ParentDashboardDto(
            int childrenCount,
            int totalSessions,
            int totalTimeMinutes,
            int currentStreakDays
    ) {}

    @Builder
    private record ChildProgressDto(
            String childId,
            double overallCompletion,
            int totalObjectives
    ) {}
}

