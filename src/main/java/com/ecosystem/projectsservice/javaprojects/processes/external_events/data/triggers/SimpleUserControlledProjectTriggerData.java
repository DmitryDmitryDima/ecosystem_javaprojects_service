package com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SimpleUserControlledProjectTriggerData implements ExternalEventData {

    // если не null, то uuid игнорируется
    private Long fileId;
}
