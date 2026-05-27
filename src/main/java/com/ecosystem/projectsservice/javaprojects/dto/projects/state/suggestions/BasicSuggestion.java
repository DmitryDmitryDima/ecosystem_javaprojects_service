package com.ecosystem.projectsservice.javaprojects.dto.projects.state.suggestions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BasicSuggestion {

    private List<SuggestedMethod> methods = new ArrayList<>();

    private List<SuggestedType> types = new ArrayList<>();


}
