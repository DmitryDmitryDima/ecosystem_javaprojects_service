package com.ecosystem.projectsservice.javaprojects.repository;

import com.ecosystem.projectsservice.javaprojects.model.File;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT file FROM File file WHERE file.id = :id")
    Optional<File> findByIdForUpdate(Long id);
}
