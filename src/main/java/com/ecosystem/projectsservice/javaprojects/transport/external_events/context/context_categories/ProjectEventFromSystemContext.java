package com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProjectEventFromSystemContext extends ExternalEventContext {



    private UUID projectId;







    // название системного процесса (опционально)
    private String origin;




}
