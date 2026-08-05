package com.Features.Admin.Building.Repository;

import com.Features.Admin.Building.model.RegisterBuildingByAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BuildingRepository extends JpaRepository<RegisterBuildingByAdmin, UUID> {
}
