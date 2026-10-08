package com.ecosystem.projectsservice.javaprojects.external_messaging.context.context_category;


import com.ecosystem.projectsservice.javaprojects.external_messaging.context.ExternalContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// автор ивента - система (к примеру - запущенный проект), попадает в комнату проекта
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMessageFromSystemContext extends ExternalContext {

    private UUID projectId;







    // название системного процесса (опционально)
    private String origin;


}
