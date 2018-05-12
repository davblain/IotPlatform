package com.gemini.iot.repository;

import com.gemini.iot.models.Device;
import com.gemini.iot.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceDao extends JpaRepository<Device,UUID> {
    List<Device> findDevicesByOwner(Group group);
}
