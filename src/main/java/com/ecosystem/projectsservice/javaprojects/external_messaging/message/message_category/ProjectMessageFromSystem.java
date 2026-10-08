package com.ecosystem.projectsservice.javaprojects.external_messaging.message.message_category;

import com.ecosystem.projectsservice.javaprojects.external_messaging.message.ExternalMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

// автор ивента - система (к примеру - запущенный проект), попадает в комнату проекта
@Getter
@Setter
@SuperBuilder
public class ProjectMessageFromSystem
        extends ExternalMessage {


}

