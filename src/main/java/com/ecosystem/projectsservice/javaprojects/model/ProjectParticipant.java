package com.ecosystem.projectsservice.javaprojects.model;

import com.ecosystem.projectsservice.javaprojects.model.enums.ParticipantRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/*
создатель проекта не должен иметь сущности participant для самого же себя
 */
@Entity
// может быть лишь одно сочетание user_uuid в participant и project id
@Table(name = "project_participants", uniqueConstraints = {@UniqueConstraint(columnNames = {"userUUID", "project_id"})})
@Getter
@Setter
@NoArgsConstructor
public class ProjectParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // uuid участника
    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userUUID;



    @Column
    @Enumerated(EnumType.STRING)
    private ParticipantRole role;

    // много участников на один проект
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project project;

}
