package com.gemini.iot.repository;

import com.gemini.iot.models.Group;
import com.gemini.iot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface GroupDao extends JpaRepository<Group,UUID> {

    List<Group> findByAdmin(User user);
}
