package com.example.cultureshelf.repository;


import java.util.UUID;

import com.example.cultureshelf.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
}
