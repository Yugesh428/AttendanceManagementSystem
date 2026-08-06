package com.Features.Admin.Subject.Service;

import com.Features.Admin.Subject.DTO.SubjectResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SubjectService {

    // ── CREATE ─────────────────────────────────────────────
    SubjectResponseDTO createSubject(SubjectResponseDTO dto);

    // ── GET ALL ────────────────────────────────────────────
    List<SubjectResponseDTO> getAllSubjects();

    // ── GET BY ID ──────────────────────────────────────────
    SubjectResponseDTO getSubjectById(UUID id);

    // ── UPDATE ─────────────────────────────────────────────
    SubjectResponseDTO updateSubject(UUID id, SubjectResponseDTO dto);

    // ── DELETE ─────────────────────────────────────────────
    void deleteSubject(UUID id);
}