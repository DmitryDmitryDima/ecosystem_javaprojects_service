package com.ecosystem.projectsservice.javaprojects.repository;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    public List<OutboxEvent> findByStatus(OutboxEvent.OutboxEventStatus status);
}
