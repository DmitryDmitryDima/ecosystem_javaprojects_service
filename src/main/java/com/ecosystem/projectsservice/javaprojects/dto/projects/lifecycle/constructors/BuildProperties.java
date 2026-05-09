package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.constructors;


import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildProperties {

    private Project project; // Transactional context

    private ProjectType projectType;

    private boolean needEntryPoint;


}
