package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email) ;
}