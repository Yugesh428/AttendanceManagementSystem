package com.Features.Admin.CourseCategory.serviceImpl;

import com.Features.Admin.CourseCategory.DTO.CourseDTO;
import com.Features.Admin.CourseCategory.excel.CourseCategoryExcelHelper;
import com.Features.Admin.CourseCategory.model.CourseCategory;
import com.Features.Admin.CourseCategory.repository.CourseCategoryRepository;
import com.Features.Admin.CourseCategory.service.CourseCategoryService;
import com.exception.ExcelImportException;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseCategoryServiceImpl implements CourseCategoryService {


    private final CourseCategoryRepository repository;



    // ── CRUD ────────────────────────────────────────────────────────────

    @Override
    public CourseDTO createCourseCategory(CourseDTO dto) {


        log.info("[COURSE CATEGORY] Creating name='{}'",
                dto.getCourseName());


        CourseCategory saved = repository.save(

                CourseCategory.builder()

                        .courseName(dto.getCourseName())

                        .courseCode(dto.getCourseCode())

                        .description(dto.getDescription())

                        .build()
        );


        log.info("[COURSE CATEGORY] Created id='{}'",
                saved.getId());


        return mapToDTO(saved);
    }





    @Override
    public CourseDTO updateCourseCategory(UUID id, CourseDTO dto) {


        log.info("[COURSE CATEGORY] Updating id='{}'",
                id);



        CourseCategory entity =
                repository.findById(id)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course Category",
                                        "id",
                                        id
                                )
                        );



        entity.setCourseName(dto.getCourseName());

        entity.setCourseCode(dto.getCourseCode());

        entity.setDescription(dto.getDescription());



        return mapToDTO(
                repository.save(entity)
        );
    }





    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseCategoryById(UUID id) {


        return mapToDTO(

                repository.findById(id)

                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course Category",
                                        "id",
                                        id
                                )
                        )
        );
    }





    @Override
    public void deleteCourseCategory(UUID id) {


        log.info("[COURSE CATEGORY] Deleting id='{}'",
                id);



        if (!repository.existsById(id)) {


            throw new ResourceNotFoundException(
                    "Course Category",
                    "id",
                    id
            );
        }



        repository.deleteById(id);
    }





    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourseCategories() {


        return repository.findAll()

                .stream()

                .map(this::mapToDTO)

                .collect(Collectors.toList());
    }





    // ── Excel Export ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToExcel() {


        log.info("[COURSE CATEGORY] Exporting courses to Excel");


        return CourseCategoryExcelHelper.export(
                getAllCourseCategories()
        );
    }





    // ── Excel Template ───────────────────────────────────────────────────

    @Override
    public ByteArrayInputStream downloadTemplate() {


        return CourseCategoryExcelHelper.template();
    }





    // ── Excel Import ─────────────────────────────────────────────────────

    @Override
    public List<CourseDTO> importFromExcel(MultipartFile file) {


        if (!CourseCategoryExcelHelper.hasExcelFormat(file)) {


            throw new ExcelImportException(

                    "Invalid file type. Please upload a .xlsx file " +
                            "(content-type: " +
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)."
            );
        }




        try {


            List<CourseDTO> parsed =
                    CourseCategoryExcelHelper.parseExcel(
                            file.getInputStream()
                    );



            log.info(
                    "[COURSE CATEGORY] Importing {} row(s) from Excel",
                    parsed.size()
            );





            return parsed.stream()



                    .map(dto ->

                            repository.save(

                                    CourseCategory.builder()

                                            .courseName(
                                                    dto.getCourseName()
                                            )

                                            .courseCode(
                                                    dto.getCourseCode()
                                            )

                                            .description(
                                                    dto.getDescription()
                                            )

                                            .build()
                            )

                    )



                    .map(this::mapToDTO)



                    .collect(Collectors.toList());




        } catch (ExcelImportException e) {


            throw e;



        } catch (IOException e) {


            throw new ExcelImportException(
                    "Could not read file: "
                            + e.getMessage()
            );
        }
    }





    // ── Mapper ───────────────────────────────────────────────────────────

    private CourseDTO mapToDTO(CourseCategory e) {


        return CourseDTO.builder()

                .id(e.getId())

                .courseName(e.getCourseName())

                .courseCode(e.getCourseCode())

                .description(e.getDescription())

                .createdAt(e.getCreatedAt())

                .updatedAt(e.getUpdatedAt())

                .build();
    }
}