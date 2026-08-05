package com.Features.Admin.CourseCategory.Controller;

import com.Features.Admin.CourseCategory.DTO.CourseDTO;
import com.Features.Admin.CourseCategory.service.CourseCategoryService;
import com.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/admin/course-categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CourseCategoryController {


    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";


    private final CourseCategoryService courseCategoryService;



    // ── CRUD ─────────────────────────────────────────────────────────────


    /** POST /api/admin/course-categories */
    @PostMapping
    public ResponseEntity<ApiResponse<CourseDTO>> createCourseCategory(
            @Valid @RequestBody CourseDTO dto) {


        CourseDTO data =
                courseCategoryService.createCourseCategory(dto);


        return ResponseEntity.status(HttpStatus.CREATED)

                .body(
                        ApiResponse.success(
                                201,
                                "Course category created successfully",
                                data
                        )
                );
    }





    /** GET /api/admin/course-categories */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getAllCourseCategories() {


        List<CourseDTO> data =
                courseCategoryService.getAllCourseCategories();



        return ResponseEntity.ok(

                ApiResponse.success(
                        "Course categories retrieved successfully",
                        data
                )
        );
    }





    /** GET /api/admin/course-categories/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseCategoryById(
            @PathVariable UUID id) {



        CourseDTO data =
                courseCategoryService.getCourseCategoryById(id);



        return ResponseEntity.ok(

                ApiResponse.success(
                        "Course category retrieved successfully",
                        data
                )
        );
    }





    /** PUT /api/admin/course-categories/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDTO>> updateCourseCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CourseDTO dto) {



        CourseDTO data =
                courseCategoryService.updateCourseCategory(id, dto);



        return ResponseEntity.ok(

                ApiResponse.success(
                        "Course category updated successfully",
                        data
                )
        );
    }





    /** DELETE /api/admin/course-categories/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourseCategory(
            @PathVariable UUID id) {



        courseCategoryService.deleteCourseCategory(id);



        return ResponseEntity.ok(

                ApiResponse.success(
                        "Course category deleted successfully"
                )
        );
    }





    // ── Excel ────────────────────────────────────────────────────────────


    /** GET /api/admin/course-categories/excel/export */
    @GetMapping("/excel/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {


        return ResponseEntity.ok()

                .contentType(
                        MediaType.parseMediaType(XLSX_MIME)
                )

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"course_categories.xlsx\""
                )

                .body(
                        new InputStreamResource(
                                courseCategoryService.exportToExcel()
                        )
                );
    }





    /** GET /api/admin/course-categories/excel/template */
    @GetMapping("/excel/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {


        return ResponseEntity.ok()

                .contentType(
                        MediaType.parseMediaType(XLSX_MIME)
                )

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"course_categories_template.xlsx\""
                )

                .body(
                        new InputStreamResource(
                                courseCategoryService.downloadTemplate()
                        )
                );
    }





    /** POST /api/admin/course-categories/excel/import */
    @PostMapping(
            value = "/excel/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<List<CourseDTO>>> importFromExcel(
            @RequestParam("file") MultipartFile file) {



        List<CourseDTO> data =
                courseCategoryService.importFromExcel(file);



        return ResponseEntity.status(HttpStatus.CREATED)

                .body(

                        ApiResponse.success(
                                201,
                                data.size()
                                        + " course categor"
                                        + (data.size() > 1 ? "ies" : "y")
                                        + " imported successfully",
                                data
                        )
                );
    }
}