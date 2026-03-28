package com.eduScale.security;

import com.eduScale.domain.User;
import com.eduScale.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParentAuthSupport {

    private final UserRepository userRepository;

    public boolean isParent(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities()
                        .contains(new SimpleGrantedAuthority(User.Role.PARENT.toSpringAuthority()));
    }

    public boolean parentOwnsChild(String parentUserId, String childUserId) {
        return userRepository.findById(childUserId)
                .filter(u -> u.getRole() == User.Role.CHILD)
                .filter(u -> parentUserId.equals(u.getParentId()))
                .isPresent();
    }

    public ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new com.eduScale.controller.ApiError(
                        "FORBIDDEN", "You are not allowed to perform this action."));
    }
}
