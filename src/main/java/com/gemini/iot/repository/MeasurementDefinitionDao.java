package com.gemini.iot.repository;

import com.gemini.iot.models.definitions.MeasurementDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasurementDefinitionDao extends JpaRepository<MeasurementDefinition,String> {
}
