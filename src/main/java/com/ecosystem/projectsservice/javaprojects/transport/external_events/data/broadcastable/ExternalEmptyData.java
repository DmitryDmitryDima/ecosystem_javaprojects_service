package com.ecosystem.projectsservice.javaprojects.transport.external_events.data.broadcastable;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class ExternalEmptyData implements ExternalEventData {
}
