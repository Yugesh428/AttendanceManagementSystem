package com.Features.Admin.Section.Repository;

import com.Features.Admin.Section.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SectionRepository extends JpaRepository<Section, UUID> {

    // 🔍 Get all sections by semester
    List<Section> findBySemesterId(UUID semesterId);

    // 🔍 Check if section already exists in same semester (optional validation)
    boolean existsByNameAndSemesterId(String name, UUID semesterId);
}