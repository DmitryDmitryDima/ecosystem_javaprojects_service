package com.ecosystem.projectsservice.javaprojects.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "directories")
@Getter
@Setter
@NoArgsConstructor
public class Directory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
