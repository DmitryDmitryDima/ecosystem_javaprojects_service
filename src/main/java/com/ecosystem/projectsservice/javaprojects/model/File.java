package com.ecosystem.projectsservice.javaprojects.model;


import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
public class File {


    @Version
    private Long version;




    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Strategy.class)
    private UUID id;

    @Column
    private String name;

    @Column
    private String extension;







    // статус файла
    @Enumerated(EnumType.STRING)
    private FileStatus status = FileStatus.AVAILABLE;


    // параметры времени

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;


    // файл нельзя перемещать/удалять
    private boolean immutable;

    // если true - полностью скрыт от пользователя
    private boolean hidden;

    // кешируем вычисленный путь
    private String constructedPath;






    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Directory parent;


    @Override
    public String toString(){
        return name+"."+extension+" "+status;
    }




}
