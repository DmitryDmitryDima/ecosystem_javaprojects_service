package com.ecosystem.projectsservice.javaprojects.dto.projects.state.suggestions;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasicSuggestionInfo {

    private int line;


    // то, что пользователь ввел
    private String userText;



    private UUID projectId;

    private UUID fileId;

    private UUID rootId;
}
