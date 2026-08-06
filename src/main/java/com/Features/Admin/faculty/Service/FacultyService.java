package com.Features.Admin.faculty.Service;

import com.Features.Admin.faculty.DTO.FacultyDTO;

import java.util.List;
import java.util.UUID;

public interface FacultyService {

    FacultyDTO createFaculty(FacultyDTO dto);

    FacultyDTO getFacultyById(UUID id);

    List<FacultyDTO> getAllFaculty();

    FacultyDTO updateFaculty(UUID id, FacultyDTO dto);

    void deleteFaculty(UUID id);
}