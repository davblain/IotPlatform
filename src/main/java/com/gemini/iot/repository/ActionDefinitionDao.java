package com.gemini.iot.repository;

import com.gemini.iot.models.definitions.ActionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionDefinitionDao extends JpaRepository<ActionDefinition,String> {
}
