package com.ecosystem.projectsservice.javaprojects.external_messaging.message.message_category;

import com.ecosystem.projectsservice.javaprojects.external_messaging.context.context_category.ProjectEventFromSystemContextCategory;
import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.ExternalMessage;
import lombok.Getter;
import lombok.Setter;

// автор ивента - система (к примеру - запущенный проект), попадает в комнату проекта
@Getter
@Setter
public class ProjectEventFromSystemCategory <D extends ExternalData>
        extends ExternalMessage<ProjectEventFromSystemContextCategory, D> {


}
