package com.ecosystem.projectsservice.javaprojects.model;


import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "directories")
@Getter
@Setter
@NoArgsConstructor
public class Directory {


    @Version
    private Long version;


    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Strategy.class)
    private UUID id;

    private String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Directory> children = new ArrayList<>();

    // в корневой папке нет родителя
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Directory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();


    @Column
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private DirectoryStatus status = DirectoryStatus.AVAILABLE;

    // иммутабельность директории - соблюдение сохранения шаблона
    private boolean immutable;

    // полностью скрыт от пользователя
    private boolean hidden;



    // кешируем вычисленный путь - ОН УЖЕ ВКЛЮЧАЕТ В СЕБЯ ИМЯ САМОЙ ПАПКИ В КОНЦЕ
    private String constructedPath;



    @Override
    public String toString(){
        return name;
    }


}
