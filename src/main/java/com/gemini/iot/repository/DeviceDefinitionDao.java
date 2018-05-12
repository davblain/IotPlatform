package com.gemini.iot.repository;

import com.gemini.iot.models.definitions.DeviceDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceDefinitionDao extends JpaRepository<DeviceDefinition,Long> {
}
