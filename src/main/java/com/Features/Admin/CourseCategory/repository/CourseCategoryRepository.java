package com.Features.Admin.CourseCategory.repository;

import com.Features.Admin.CourseCategory.model.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseCategoryRepository extends JpaRepository<CourseCategory, UUID> {


    Optional<CourseCategory> findByCourseName(String courseName);


    Optional<CourseCategory> findByCourseCode(String courseCode);


    boolean existsByCourseName(String courseName);


    boolean existsByCourseCode(String courseCode);


    boolean existsByCourseNameAndIdNot(String courseName, UUID id);


    boolean existsByCourseCodeAndIdNot(String courseCode, UUID id);
}