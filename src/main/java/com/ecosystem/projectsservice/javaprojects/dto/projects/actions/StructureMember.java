package com.ecosystem.projectsservice.javaprojects.dto.projects.actions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class StructureMember {

    // directory_id or file_id
    private String id;

    private String name;

    // directory or file
    private String type;

    private boolean immutable;



    private List<StructureMember> children = new ArrayList<>();
}
