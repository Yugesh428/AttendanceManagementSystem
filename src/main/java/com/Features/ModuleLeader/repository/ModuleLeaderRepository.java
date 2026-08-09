package com.Features.ModuleLeader.repository;

import com.Features.ModuleLeader.model.ModuleLeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleLeaderRepository extends JpaRepository<ModuleLeader, UUID> {

    boolean existsByEmail(String email);

    Optional<ModuleLeader> findByEmail(String email);

    /** Check if a subject already has a module leader */
    boolean existsBySubjectId(UUID subjectId);
}
