package com.eduScale.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    public enum Role {
        PARENT,
        CHILD
    }

    @Id
    private String id;

    @Indexed
    private String parentId;

    private String name;

    private int age;

    private String grade;

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

