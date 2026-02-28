package com.ecosystem.projectsservice.javaprojects.processes.external_events.data;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonSerialize
public class ExternalEmptyData implements ExternalEventData {
}
