package com.Features.Teacher.service;

import com.Features.Teacher.dto.TeacherDTO;
import com.Features.Teacher.dto.TeacherLoginRequest;
import com.Features.Teacher.dto.TeacherLoginResponse;

import java.util.List;
import java.util.UUID;

public interface TeacherService {

    /**
     * Admin registers a Faculty member as a Teacher.
     * Auto-creates login account and returns generatedPassword (shown once).
     */
    TeacherDTO registerTeacher(TeacherDTO dto);

    TeacherDTO getTeacherById(UUID id);

    List<TeacherDTO> getAllTeachers();

    TeacherDTO updateTeacher(UUID id, TeacherDTO dto);

    void deleteTeacher(UUID id);

    /** Teacher login — returns JWT with ROLE_TEACHER */
    TeacherLoginResponse login(TeacherLoginRequest request);

    /** Teacher views their own profile */
    TeacherDTO getMyProfile(String email);
}
