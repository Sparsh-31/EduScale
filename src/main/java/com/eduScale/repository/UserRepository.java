package com.eduScale.repository;

import com.eduScale.domain.User;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    List<User> findByParentId(String parentId);
}

