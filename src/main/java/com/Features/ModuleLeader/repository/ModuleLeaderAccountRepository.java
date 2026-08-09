package com.Features.ModuleLeader.repository;

import com.Features.ModuleLeader.model.ModuleLeaderAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleLeaderAccountRepository extends JpaRepository<ModuleLeaderAccount, UUID> {

    Optional<ModuleLeaderAccount> findByUsername(String username);

    boolean existsByUsername(String username);
}
