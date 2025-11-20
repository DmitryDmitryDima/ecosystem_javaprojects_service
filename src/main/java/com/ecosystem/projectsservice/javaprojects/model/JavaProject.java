package com.ecosystem.projectsservice.javaprojects.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "java_projects", uniqueConstraints = {@UniqueConstraint(columnNames = {"userUUID", "name"})})
@Getter
@Setter
@NoArgsConstructor
public class JavaProject {

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

    @Column
    private Boolean running;

    // File entry point

    // Directory root


}
