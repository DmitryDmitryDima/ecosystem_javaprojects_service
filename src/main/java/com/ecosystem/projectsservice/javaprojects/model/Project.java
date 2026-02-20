package com.ecosystem.projectsservice.javaprojects.model;


import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectPrivacyLevel;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "java_projects", uniqueConstraints = {@UniqueConstraint(columnNames = {"userUUID", "name"})})
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // пара uuid и name - уникальна
    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userUUID;

    @Column(nullable = false)
    private String name;

    @Column
    private Instant createdAt;





    /*
    три состояния проекта - AVAILABLE, REMOVING, RUNNING
     */
    @Column
    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.AVAILABLE;

    /*
    уровень приватности
     */
    @Column
    @Enumerated(EnumType.STRING)
    private ProjectPrivacyLevel privacyLevel = ProjectPrivacyLevel.OPEN;

    /*
    от типа проекта может зависеть алгоритм запуска, а также алгоритм формирования dto, поэтому его стоит вынести в модель
     */
    @Column
    @Enumerated(EnumType.STRING)
    private ProjectType type = ProjectType.MAVEN_CLASSIC;

    // File entry point
    // главный файл проекта
    @OneToOne
    @JoinColumn(name = "entry_point_id", referencedColumnName = "id")
    private File entryPoint;

    // корневая папа проекта - не имеет родителей
    // orphan removal означает удаление детей вместе с родителем
    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "root_id", referencedColumnName = "id")
    private Directory root;



    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectParticipant> participants = new ArrayList<>();


}
