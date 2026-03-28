package com.eduScale.controller;

import com.eduScale.domain.Chapter;
import com.eduScale.domain.Curriculum;
import com.eduScale.domain.Grade;
import com.eduScale.domain.LearningObjective;
import com.eduScale.domain.Subject;
import com.eduScale.domain.User;
import com.eduScale.repository.ChapterRepository;
import com.eduScale.repository.CurriculumRepository;
import com.eduScale.repository.GradeRepository;
import com.eduScale.repository.LearningObjectiveRepository;
import com.eduScale.repository.SubjectRepository;
import com.eduScale.repository.UserRepository;
import com.eduScale.security.ParentAuthSupport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumRepository curriculumRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final LearningObjectiveRepository learningObjectiveRepository;
    private final UserRepository userRepository;
    private final ParentAuthSupport parentAuthSupport;

    @GetMapping("/curriculums")
    public ResponseEntity<?> listCurriculums(Authentication authentication) {
        if (!parentAuthSupport.isParent(authentication)) {
            return parentAuthSupport.forbidden();
        }
        return ResponseEntity.ok(curriculumRepository.findAllByOrderById());
    }

    @GetMapping("/curriculums/{curriculumId}/grades")
    public ResponseEntity<?> listGrades(Authentication authentication, @PathVariable String curriculumId) {
        if (!parentAuthSupport.isParent(authentication)) {
            return parentAuthSupport.forbidden();
        }
        return ResponseEntity.ok(gradeRepository.findByCurriculumIdOrderByOrderAsc(curriculumId));
    }

    @GetMapping("/grades/{gradeId}/subjects")
    public ResponseEntity<?> listSubjects(Authentication authentication, @PathVariable String gradeId) {
        if (!parentAuthSupport.isParent(authentication)) {
            return parentAuthSupport.forbidden();
        }
        return ResponseEntity.ok(subjectRepository.findByGradeId(gradeId));
    }

    @GetMapping("/subjects/{subjectId}/chapters")
    public ResponseEntity<?> listChapters(Authentication authentication, @PathVariable String subjectId) {
        if (!parentAuthSupport.isParent(authentication)) {
            return parentAuthSupport.forbidden();
        }
        return ResponseEntity.ok(chapterRepository.findBySubjectId(subjectId));
    }

    @GetMapping("/chapters/{chapterId}/objectives")
    public ResponseEntity<?> listLearningObjectives(
            Authentication authentication,
            @PathVariable String chapterId) {
        if (!parentAuthSupport.isParent(authentication)) {
            return parentAuthSupport.forbidden();
        }
        return ResponseEntity.ok(learningObjectiveRepository.findByChapterId(chapterId));
    }

    /**
     * Get all learning objectives available for a child, based on the child's assigned grade.
     * If the child has no grade assigned, returns empty list.
     * Traverses: child.gradeId → subjects in that grade → chapters in those subjects → objectives in those chapters.
     */
    @GetMapping("/children/{childId}/learning-objectives")
    public ResponseEntity<?> getLearningObjectivesForChild(
            Authentication authentication,
            @PathVariable String childId) {
        if (!parentAuthSupport.isParent(authentication)
                || !parentAuthSupport.parentOwnsChild(authentication.getName(), childId)) {
            return parentAuthSupport.forbidden();
        }
        return userRepository.findById(childId)
                .filter(user -> user.getRole() == User.Role.CHILD)
                .map(child -> {
                    String gradeId = child.getGrade();
                    if (gradeId == null || gradeId.isBlank()) {
                        return ResponseEntity.ok(List.<LearningObjective>of());
                    }
                    List<Subject> subjects = subjectRepository.findByGradeId(gradeId);
                    if (subjects.isEmpty()) {
                        return ResponseEntity.ok(List.<LearningObjective>of());
                    }

                    List<String> subjectIds = subjects.stream().map(Subject::getId).toList();
                    List<Chapter> chapters = chapterRepository.findBySubjectIdIn(subjectIds);
                    if (chapters.isEmpty()) {
                        return ResponseEntity.ok(List.<LearningObjective>of());
                    }

                    List<String> chapterIds = chapters.stream().map(Chapter::getId).toList();
                    List<LearningObjective> objectives = learningObjectiveRepository.findByChapterIdIn(chapterIds);
                    return ResponseEntity.ok(objectives);
                })
                .orElse(ResponseEntity.<List<LearningObjective>>notFound().build());
    }
}
