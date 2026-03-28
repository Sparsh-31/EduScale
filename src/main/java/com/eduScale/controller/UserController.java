package com.eduScale.controller;

import com.eduScale.domain.User;
import com.eduScale.repository.UserRepository;
import com.eduScale.security.ParentAuthSupport;
import jakarta.validation.constraints.Email;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParentAuthSupport parentAuthSupport;

    private boolean canParentOrSelfRead(Authentication auth, String userId) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (auth.getName().equals(userId)) {
            return true;
        }
        if (parentAuthSupport.isParent(auth)) {
            return parentAuthSupport.parentOwnsChild(auth.getName(), userId);
        }
        return false;
    }

    /**
     * Create a parent user (no parentId, role = PARENT). Public — no JWT.
     */
    @PostMapping("/parents")
    public ResponseEntity<?> createParent(@Valid @RequestBody CreateParentRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError("EMAIL_EXISTS", "A user with this email already exists."));
        }

        User parent = User.builder()
                .parentId(null)
                .name(request.getName())
                .age(request.getAge() != null ? request.getAge() : 0)
                .grade(null)
                .email(normalizedEmail)
                .role(User.Role.PARENT)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        parent = userRepository.save(parent);
        return ResponseEntity.status(HttpStatus.CREATED).body(parent);
    }

    /**
     * Create a child user. Authenticated parent only; parentId must match JWT subject.
     */
    @PostMapping("/children")
    public ResponseEntity<?> createChild(
            Authentication authentication,
            @Valid @RequestBody CreateChildRequest request) {
        if (!parentAuthSupport.isParent(authentication)) {
            return parentAuthSupport.forbidden();
        }
        if (!authentication.getName().equals(request.getParentId())) {
            return parentAuthSupport.forbidden();
        }
        String childPassword = request.getPassword() == null || request.getPassword().isBlank()
                ? "demo"
                : request.getPassword();
        User child = User.builder()
                .parentId(request.getParentId())
                .name(request.getName())
                .age(request.getAge() != null ? request.getAge() : 0)
                .grade(request.getGradeId())
                .email(null)
                .role(User.Role.CHILD)
                .password(passwordEncoder.encode(childPassword))
                .build();
        child = userRepository.save(child);
        return ResponseEntity.status(HttpStatus.CREATED).body(child);
    }

    /**
     * List children for the authenticated parent. parentId query must equal JWT subject.
     */
    @GetMapping
    public ResponseEntity<?> listUsers(
            Authentication authentication,
            @RequestParam(required = false) String parentId) {
        if (!parentAuthSupport.isParent(authentication)) {
            return parentAuthSupport.forbidden();
        }
        if (parentId == null || parentId.isBlank() || !parentId.equals(authentication.getName())) {
            return parentAuthSupport.forbidden();
        }
        return ResponseEntity.ok(userRepository.findByParentId(parentId));
    }

    /**
     * Get a single user by id (self or parent viewing own child).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(Authentication authentication, @PathVariable String userId) {
        if (!canParentOrSelfRead(authentication, userId)) {
            return parentAuthSupport.forbidden();
        }
        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a user (self or parent updating own child).
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            Authentication authentication,
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request) {
        if (!canParentOrSelfRead(authentication, userId)) {
            return parentAuthSupport.forbidden();
        }
        return userRepository.findById(userId)
                .map(user -> {
                    if (request.getName() != null) {
                        user.setName(request.getName());
                    }
                    if (request.getAge() != null) {
                        user.setAge(request.getAge());
                    }
                    if (request.getGradeId() != null) {
                        user.setGrade(request.getGradeId());
                    }
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DTOs
    public static class CreateParentRequest {
        @NotBlank
        private String name;
        @NotBlank
        @Email
        private String email;
        private Integer age;
        @NotBlank
        @Size(min = 6, message = "must be at least 6 characters")
        private String password;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class CreateChildRequest {
        @NotBlank
        private String parentId;
        @NotBlank
        private String name;
        private Integer age;
        private String gradeId;
        private String password;

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getGradeId() {
            return gradeId;
        }

        public void setGradeId(String gradeId) {
            this.gradeId = gradeId;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class UpdateUserRequest {
        private String name;
        private Integer age;
        private String gradeId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getGradeId() {
            return gradeId;
        }

        public void setGradeId(String gradeId) {
            this.gradeId = gradeId;
        }
    }
}
