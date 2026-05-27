package com.ecosystem.projectsservice.javaprojects.dto.projects.state.suggestions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// параметры подсказки
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicSuggestionCriteria {

    private int line;


    // то, что пользователь ввел
    private String userText;
}
