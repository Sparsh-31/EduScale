package com.eduScale.controller;

import com.eduScale.domain.User;
import com.eduScale.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * Create a parent user (no parentId, role = PARENT).
     */
    @PostMapping("/parents")
    public ResponseEntity<User> createParent(@Valid @RequestBody CreateParentRequest request) {
        User parent = User.builder()
                .parentId(null)
                .name(request.getName())
                .age(request.getAge() != null ? request.getAge() : 0)
                .grade(null)
                .role(User.Role.PARENT)
                .password(request.getPassword())
                .build();
        parent = userRepository.save(parent);
        return ResponseEntity.status(HttpStatus.CREATED).body(parent);
    }

    /**
     * Create a child user (parentId required, role = CHILD). Optionally set gradeId so the child sees learning objectives for that grade.
     */
    @PostMapping("/children")
    public ResponseEntity<User> createChild(@Valid @RequestBody CreateChildRequest request) {
        User child = User.builder()
                .parentId(request.getParentId())
                .name(request.getName())
                .age(request.getAge() != null ? request.getAge() : 0)
                .grade(request.getGradeId())
                .role(User.Role.CHILD)
                .password(request.getPassword())
                .build();
        child = userRepository.save(child);
        return ResponseEntity.status(HttpStatus.CREATED).body(child);
    }

    /**
     * Demo parent login. No tokens yet: it just validates (parentId + password).
     *
     * Compatibility note:
     * - Older DB records may not have a password yet (null). For those records,
     *   we treat any provided password as valid in this demo phase.
     */
    @PostMapping("/parents/login")
    public ResponseEntity<User> loginParent(@Valid @RequestBody LoginParentRequest request) {
        return (ResponseEntity<User>) userRepository.findById(request.getParentId())
                .map(user -> {
                    if (user.getRole() != User.Role.PARENT) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }

                    String storedPassword = user.getPassword();
                    boolean legacyNoPassword = storedPassword == null || storedPassword.isBlank();
                    boolean passwordMatches =
                            legacyNoPassword || storedPassword.equals(request.getPassword());

                    if (!passwordMatches) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }

                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * List children for a parent.
     */
    @GetMapping
    public ResponseEntity<List<User>> listUsers(
            @RequestParam(required = false) String parentId) {
        if (parentId != null && !parentId.isBlank()) {
            return ResponseEntity.ok(userRepository.findByParentId(parentId));
        }
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Get a single user by id.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a user (e.g. assign or change grade for a child).
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (request.getName() != null) user.setName(request.getName());
                    if (request.getAge() != null) user.setAge(request.getAge());
                    if (request.getGradeId() != null) user.setGrade(request.getGradeId());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DTOs
    public static class CreateParentRequest {
        @NotBlank
        private String name;
        private Integer age;
        @NotBlank
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class CreateChildRequest {
        @NotBlank
        private String parentId;
        @NotBlank
        private String name;
        private Integer age;
        private String gradeId;
        @NotBlank
        private String password;

        public String getParentId() { return parentId; }
        public void setParentId(String parentId) { this.parentId = parentId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getGradeId() { return gradeId; }
        public void setGradeId(String gradeId) { this.gradeId = gradeId; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginParentRequest {
        @NotBlank
        private String parentId;
        @NotBlank
        private String password;

        public String getParentId() { return parentId; }
        public void setParentId(String parentId) { this.parentId = parentId; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UpdateUserRequest {
        private String name;
        private Integer age;
        private String gradeId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getGradeId() { return gradeId; }
        public void setGradeId(String gradeId) { this.gradeId = gradeId; }
    }
}
