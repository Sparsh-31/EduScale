package com.eduScale.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Locale;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    public enum Role {
        PARENT,
        CHILD;

        /** Spring Security {@code GrantedAuthority} string for this role. */
        public String toSpringAuthority() {
            return "ROLE_" + name();
        }

        /**
         * Parses role strings from JWT or other clients: {@code PARENT}, {@code parent},
         * or {@code ROLE_PARENT} (case-insensitive).
         */
        public static Optional<Role> fromTokenClaim(String raw) {
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            String normalized = raw.trim().toUpperCase(Locale.ROOT);
            if (normalized.startsWith("ROLE_")) {
                normalized = normalized.substring(5);
            }
            try {
                return Optional.of(Role.valueOf(normalized));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    @Id
    private String id;

    @Indexed
    private String parentId;

    private String name;

    private int age;

    private String grade;

    @Indexed(unique = true, sparse = true)
    private String email;

    private Role role;

    /**
     * Demo password field for the upcoming login flow.
     * <p>
     * NOTE: In the first iteration we store the value as-is.
     * In a later security task we should hash it (e.g. BCrypt) and add real auth.
     */
    @JsonIgnore
    private String password;
}

