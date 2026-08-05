package com.Features.Admin.Beacon.Repository;

import com.Features.Admin.Beacon.Beacon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BeaconRepository extends JpaRepository<Beacon, UUID> {
    List<Beacon> findByClassroomId(UUID classroomId);
}
