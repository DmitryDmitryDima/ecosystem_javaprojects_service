package com.ecosystem.projectsservice.javaprojects.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.time.Instant;
import java.util.UUID;

/*
токен может быть использован только один раз
имеет время устаревания
как только пользователь переходит по ссылке, проставляется флаг user

предназначается для добавления participant в проект



 */
@Entity
@Table(name = "java_projects_invite_tokens")
@Getter
@Setter
@NoArgsConstructor
public class ProjectInviteToken {

    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Strategy.class)
    private UUID id;

    private Instant expiredAt = Instant.now().plusSeconds(2*60);

    private boolean used;

    // токен может быть персональным
    @Column(nullable = true)
    private UUID userUUID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project project;



}
