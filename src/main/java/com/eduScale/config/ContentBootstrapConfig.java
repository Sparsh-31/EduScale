package com.eduScale.config;

import com.eduScale.domain.*;
import com.eduScale.repository.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class ContentBootstrapConfig implements CommandLineRunner {

    private final LearningObjectiveRepository objectiveRepository;
    private final ActivityRepository activityRepository;
    private final ActivityPoolRepository activityPoolRepository;
    private final ContentPackRepository contentPackRepository;
    private final CurriculumRepository curriculumRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (curriculumRepository.count() == 0) {
            curriculumRepository.saveAll(readJson(
                    "content/curriculums.json",
                    new TypeReference<List<Curriculum>>() {}
            ));
        }
        if (gradeRepository.count() == 0) {
            gradeRepository.saveAll(readJson(
                    "content/grades.json",
                    new TypeReference<List<Grade>>() {}
            ));
        }
        if (subjectRepository.count() == 0) {
            subjectRepository.saveAll(readJson(
                    "content/subjects.json",
                    new TypeReference<List<Subject>>() {}
            ));
        }
        if (chapterRepository.count() == 0) {
            chapterRepository.saveAll(readJson(
                    "content/chapters.json",
                    new TypeReference<List<Chapter>>() {}
            ));
        }
        if (objectiveRepository.count() == 0) {
            objectiveRepository.saveAll(readJson(
                    "content/learning_objectives.json",
                    new TypeReference<List<LearningObjective>>() {}
            ));
        }

        if (activityRepository.count() == 0) {
            activityRepository.saveAll(readJson(
                    "content/activities.json",
                    new TypeReference<List<Activity>>() {}
            ));
        }

        if (activityPoolRepository.count() == 0) {
            activityPoolRepository.saveAll(readJson(
                    "content/activity_pools.json",
                    new TypeReference<List<ActivityPool>>() {}
            ));
        }

        if (contentPackRepository.count() == 0) {
            contentPackRepository.saveAll(readJson(
                    "content/content_packs.json",
                    new TypeReference<List<ContentPack>>() {}
            ));
        }

        validateHierarchyReferences();
    }

    private <T> T readJson(String path, TypeReference<T> type) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return objectMapper.readValue(is, type);
        }
    }

    /**
     * Guardrail validation so seeded/manual data cannot silently drift.
     */
    private void validateHierarchyReferences() {
        Set<String> curriculumIds = curriculumRepository.findAll().stream()
                .map(Curriculum::getId)
                .collect(Collectors.toSet());
        Set<String> gradeIds = gradeRepository.findAll().stream()
                .map(Grade::getId)
                .collect(Collectors.toSet());
        Set<String> subjectIds = subjectRepository.findAll().stream()
                .map(Subject::getId)
                .collect(Collectors.toSet());
        Set<String> chapterIds = chapterRepository.findAll().stream()
                .map(Chapter::getId)
                .collect(Collectors.toSet());
        Set<String> objectiveIds = objectiveRepository.findAll().stream()
                .map(LearningObjective::getId)
                .collect(Collectors.toSet());
        Set<String> activityIds = activityRepository.findAll().stream()
                .map(Activity::getId)
                .collect(Collectors.toSet());

        Set<String> missingGradeCurriculums = gradeRepository.findAll().stream()
                .map(Grade::getCurriculumId)
                .filter(id -> id != null && !id.isBlank() && !curriculumIds.contains(id))
                .collect(Collectors.toCollection(HashSet::new));
        if (!missingGradeCurriculums.isEmpty()) {
            throw new IllegalStateException("Invalid grades: missing curriculum IDs " + missingGradeCurriculums);
        }

        Set<String> missingSubjectGrades = subjectRepository.findAll().stream()
                .map(Subject::getGradeId)
                .filter(id -> id != null && !id.isBlank() && !gradeIds.contains(id))
                .collect(Collectors.toCollection(HashSet::new));
        if (!missingSubjectGrades.isEmpty()) {
            throw new IllegalStateException("Invalid subjects: missing grade IDs " + missingSubjectGrades);
        }

        Set<String> missingChapterSubjects = chapterRepository.findAll().stream()
                .map(Chapter::getSubjectId)
                .filter(id -> id != null && !id.isBlank() && !subjectIds.contains(id))
                .collect(Collectors.toCollection(HashSet::new));
        if (!missingChapterSubjects.isEmpty()) {
            throw new IllegalStateException("Invalid chapters: missing subject IDs " + missingChapterSubjects);
        }

        Set<String> missingObjectiveChapters = objectiveRepository.findAll().stream()
                .map(LearningObjective::getChapterId)
                .filter(id -> id != null && !id.isBlank() && !chapterIds.contains(id))
                .collect(Collectors.toCollection(HashSet::new));
        if (!missingObjectiveChapters.isEmpty()) {
            throw new IllegalStateException("Invalid objectives: missing chapter IDs " + missingObjectiveChapters);
        }

        Set<String> missingPoolObjectives = activityPoolRepository.findAll().stream()
                .map(ActivityPool::getObjectiveId)
                .filter(id -> id != null && !id.isBlank() && !objectiveIds.contains(id))
                .collect(Collectors.toCollection(HashSet::new));
        if (!missingPoolObjectives.isEmpty()) {
            throw new IllegalStateException("Invalid activity pools: missing objective IDs " + missingPoolObjectives);
        }

        Set<String> missingPoolActivities = activityPoolRepository.findAll().stream()
                .flatMap(pool -> pool.getActivityIds() == null ? Set.<String>of().stream() : pool.getActivityIds().stream())
                .filter(id -> id != null && !id.isBlank() && !activityIds.contains(id))
                .collect(Collectors.toCollection(HashSet::new));
        if (!missingPoolActivities.isEmpty()) {
            throw new IllegalStateException("Invalid activity pools: missing activity IDs " + missingPoolActivities);
        }

        Set<String> missingActivityObjectives = activityRepository.findAll().stream()
                .flatMap(activity -> activity.getObjectiveIds() == null ? Set.<String>of().stream() : activity.getObjectiveIds().stream())
                .filter(id -> id != null && !id.isBlank() && !objectiveIds.contains(id))
                .collect(Collectors.toCollection(HashSet::new));
        if (!missingActivityObjectives.isEmpty()) {
            throw new IllegalStateException("Invalid activities: missing objective IDs " + missingActivityObjectives);
        }
    }
}