package com.Features.Admin.CourseCategory.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "course_category",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_name",
                        columnNames = "course_name"
                ),
                @UniqueConstraint(
                        name = "uk_course_code",
                        columnNames = "course_code"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCategory {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            updatable = false,
            nullable = false
    )
    private UUID id;



    @Column(
            name = "course_name",
            nullable = false,
            length = 100
    )
    private String courseName;



    @Column(
            name = "course_code",
            nullable = false,
            length = 100
    )
    private String courseCode;



    @Column(
            name = "description",
            length = 255
    )
    private String description;



    @CreationTimestamp
    @Column(
            name = "created_at",
            updatable = false
    )
    private LocalDateTime createdAt;



    @UpdateTimestamp
    @Column(
            name = "updated_at"
    )
    private LocalDateTime updatedAt;
}