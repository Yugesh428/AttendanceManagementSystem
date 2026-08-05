package com.Features.Admin.CourseCategory.service;

import com.Features.Admin.CourseCategory.DTO.CourseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface CourseCategoryService {


    CourseDTO createCourseCategory(CourseDTO dto);


    CourseDTO updateCourseCategory(UUID id, CourseDTO dto);


    CourseDTO getCourseCategoryById(UUID id);


    void deleteCourseCategory(UUID id);


    List<CourseDTO> getAllCourseCategories();



    // Excel
    ByteArrayInputStream exportToExcel();


    ByteArrayInputStream downloadTemplate();


    List<CourseDTO> importFromExcel(MultipartFile file);
}