package com.Features.Admin.Classroom.ClassroomService;

import com.Features.Admin.Classroom.DTO.ClassroomDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface ClassroomService {
    ClassroomDTO createClassroom(ClassroomDTO dto);
    ClassroomDTO updateClassroom(UUID id, ClassroomDTO dto);
    void deleteClassroom(UUID id);
    List<ClassroomDTO> findAllClassrooms();
    ClassroomDTO findClassroomById(UUID id);

    // ── Excel ──────────────────────────────────────────────────────────────────
    ByteArrayInputStream exportToExcel();
    ByteArrayInputStream downloadTemplate();
    List<ClassroomDTO> importFromExcel(MultipartFile file);
}
