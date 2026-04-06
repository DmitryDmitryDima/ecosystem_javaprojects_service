package com.ecosystem.projectsservice.javaprojects.dto.projects.access_validation;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


// несет в себе все данные, что необходимы другим сервисам после валидации
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationInfo {
    private UUID projectOwner;
}
