package com.stasdev.backend.model.repos;

import com.stasdev.backend.model.entitys.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {

    ApplicationUser findByUsername(String username);

    Optional<ApplicationUser> getApplicationUserByUsername(String username);
}
