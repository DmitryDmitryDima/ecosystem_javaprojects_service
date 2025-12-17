package com.ecosystem.projectsservice.javaprojects.dto.projects.actions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StructureMember {

    // directory_id or file_id - поле для фронтенда. чтобы не было ситуации, когда id папки равен id файла
    private String id;

    // сохраняем это поле для настройки
    private Long originalId;

    private String name;

    // directory or file
    private String type;

    private boolean immutable;





    private List<StructureMember> children = new ArrayList<>();

    @Override
    public String toString(){
        return type+" "+name+" " +children;
    }
}
