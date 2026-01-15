package com.ecosystem.projectsservice.javaprojects.repository;

import com.ecosystem.projectsservice.javaprojects.model.Project;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {


    Optional<Project> findByNameAndUserUUID(String name, UUID userUUID);

    List<Project> findByUserUUID(UUID userUUID);


    // пессимистичная блокировка - запись в бд блокируется на момент транзакции
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p from Project p where p.id=?1")
    Optional<Project> findByIdForUpdate(Long id);


}
