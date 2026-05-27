package com.ecosystem.projectsservice.javaprojects.dto.projects.state.suggestions;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SuggestedType {


    private String path;

    private String name;


}
