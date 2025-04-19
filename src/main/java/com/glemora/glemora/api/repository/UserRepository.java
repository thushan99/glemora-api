package com.glemora.glemora.api.repository;


import com.glemora.glemora.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsername(String username);
    User findByEmail(String email);

    @Query("SELECT r.name FROM User u JOIN u.roles r WHERE u.username = :username")
    List<String> findRolesByUsername(String username);
}
