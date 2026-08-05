package com.Features.Admin.Semester;





import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "semesters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {

    @Id
    @GeneratedValue
    private UUID id;

    private String name; // e.g. "Semester 1"

    private LocalDateTime createdAt;
}