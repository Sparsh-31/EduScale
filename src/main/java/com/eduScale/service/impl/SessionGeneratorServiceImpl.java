package com.eduScale.service.impl;

import com.eduScale.domain.Activity;
import com.eduScale.domain.ActivityPool;
import com.eduScale.domain.ActivityType;
import com.eduScale.domain.EngineType;
import com.eduScale.domain.Session;
import com.eduScale.repository.ActivityPoolRepository;
import com.eduScale.repository.ActivityRepository;
import com.eduScale.repository.SessionRepository;
import com.eduScale.repository.UserActivityHistoryRepository;
import com.eduScale.service.SessionGeneratorService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionGeneratorServiceImpl implements SessionGeneratorService {

    private final ActivityRepository activityRepository;
    private final ActivityPoolRepository activityPoolRepository;
    private final UserActivityHistoryRepository historyRepository;
    private final SessionRepository sessionRepository;

    @Override
    public Session startSession(String userId, String objectiveId) {
        List<String> activityIds = generateActivitySequence(userId, objectiveId);

        Session session = Session.builder()
                .userId(userId)
                .objectiveId(objectiveId)
                .activityIds(activityIds)
                .completed(false)
                .startedAt(Instant.now())
                .build();

        return sessionRepository.save(session);
    }

    @Override
    public List<String> generateActivitySequence(String userId, String objectiveId) {
        List<ActivityPool> pools = activityPoolRepository.findByObjectiveId(objectiveId);
        Set<String> poolActivityIds = pools.stream()
                .flatMap(p -> p.getActivityIds().stream())
                .collect(Collectors.toSet());

        List<Activity> allActivities = activityRepository.findAllById(poolActivityIds);

        List<Activity> learningCandidates = filterByTypeAndEngine(allActivities, ActivityType.LEARNING, EngineType.OBSERVE);
        List<Activity> practiceCandidates = filterByType(allActivities, ActivityType.PRACTICE);
        List<Activity> assessmentCandidates = filterByType(allActivities, ActivityType.ASSESSMENT);

        List<String> sequence = new ArrayList<>();

        pickOneAvoidingRecent(userId, learningCandidates, sequence, 0);
        pickOneAvoidingRecent(userId, practiceCandidates, sequence, 1);
        pickOneAvoidingRecent(userId, practiceCandidates, sequence, 2);
        pickOneAvoidingRecent(userId, assessmentCandidates, sequence, 3);

        return sequence;
    }

    private List<Activity> filterByType(List<Activity> activities, ActivityType type) {
        return activities.stream()
                .filter(a -> a.getActivityType() == type)
                .collect(Collectors.toList());
    }

    private List<Activity> filterByTypeAndEngine(List<Activity> activities, ActivityType type, EngineType engineType) {
        return activities.stream()
                .filter(a -> a.getActivityType() == type && a.getEngineType() == engineType)
                .collect(Collectors.toList());
    }

    private void pickOneAvoidingRecent(String userId, List<Activity> candidates, List<String> sequence, int position) {
        if (candidates.isEmpty()) {
            return;
        }
        // TODO: use historyRepository to implement avoidRepeat/avoidRecent/maxSameEngine rules.
        Activity chosen = candidates.get(position % candidates.size());
        sequence.add(chosen.getId());
    }
}

