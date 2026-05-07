package com.ecosystem.projectsservice.javaprojects.repository;

import com.ecosystem.projectsservice.javaprojects.model.Project;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {


    Optional<Project> findByNameAndUserUUID(String name, UUID userUUID);

    List<Project> findByUserUUID(UUID userUUID);


    // пессимистичная блокировка - запись в бд блокируется на момент транзакции
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p from Project p where p.id=?1")
    Optional<Project> findByIdForUpdate(UUID id);

    /*суть данного запроса (важно - target!=caller) в том, что он позволяет извлечь:

    а) открытые проекты, в котором target является участником
    б) приватные проекты, в которых caller и target оба являются участниками и поэтому caller видит этот приватный проект

    Держим в голове, что автор проекта не является его участником с точки зрения бд
    - для него данная сущность не создается
     */


    @NativeQuery("select java.id, java.created_at, java.name, java.status, java.useruuid, java.privacy_level,java.entry_point_id, java.root_id, java.type," +
            "count(*) " +
            "from java_projects java inner join project_participants part on part.project_id=java.id " +
            "and (part.useruuid= :target or part.useruuid= :caller) " +
            "where java.privacy_level='PRIVATE' " +
            "group by java.id, java.created_at, java.name, java.status, java.useruuid, java.privacy_level, " +
            "java.entry_point_id, java.root_id, java.type " +
            "having count(*)>1 " +
            "UNION  " +
            "select java.id, java.created_at, java.name, java.status, java.useruuid, java.privacy_level, " +
            "java.entry_point_id, java.root_id, java.type, " +
            "count(*) " +
            "from java_projects java inner join project_participants part on part.project_id=java.id " +
            "AND part.useruuid= :target " +
            "where java.privacy_level='OPEN' " +
            "group by java.id, java.created_at, java.name, java.status, java.useruuid, java.privacy_level, " +
            "java.entry_point_id, java.root_id, java.type;")
    List<Project> readAllParticipantProjectsByDifferentTargetAndCaller(@Param("target") UUID target, @Param("caller") UUID caller);

    @NativeQuery("select java.id, java.created_at, java.name, java.status, java.useruuid, " +
            "java.privacy_level,java.entry_point_id, java.root_id, java.type " +
            "from java_projects java inner join project_participants part on part.project_id=java.id and part.useruuid= :target")
    List<Project> readAllProjectsByParticipant(@Param("target") UUID target);




}
