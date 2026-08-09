package com.Features.ModuleLeader.model;

import com.Features.Admin.Subject.Subject;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A Module Leader is responsible for one Subject.
 * Created by Admin — gets their own login account (email + password emailed on creation).
 *
 * Access scope:
 *   - Can view all TimetableSlots for their subject
 *   - Can view attendance reports / student % for their subject
 *   - Can see which teachers are covering their subject
 *   - Cannot mark, override attendance, or manage any data
 */
@Entity
@Table(name = "module_leaders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleLeader {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    /** The subject this module leader is responsible for */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Builder.Default
    private boolean active = true;

    /** Login account — created automatically when module leader is registered */
    @OneToOne(mappedBy = "moduleLeader", cascade = CascadeType.ALL,
              fetch = FetchType.LAZY, orphanRemoval = true)
    private ModuleLeaderAccount account;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
