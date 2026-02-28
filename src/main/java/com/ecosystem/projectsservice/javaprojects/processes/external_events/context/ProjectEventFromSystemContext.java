package com.ecosystem.projectsservice.javaprojects.processes.external_events.context;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProjectEventFromSystemContext extends ExternalEventContext{



    private UUID projectId;

    // добавляем это поле для более умного routing'а
    private UUID projectAuthor;

    // если opened, то ивент пересылается в том числе подписчикам user public канала (пример - добавление/удаление участника в открытый проект)
    // для этого имеем поле автор
    private boolean opened;



    // название системного процесса (опционально)
    private String origin;



    // участники проекта - опционально для случаев, где требуется персональная рассылка участникам проекта
    private List<UUID> participants;
}
