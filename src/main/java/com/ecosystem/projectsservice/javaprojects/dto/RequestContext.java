package com.ecosystem.projectsservice.javaprojects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestContext {
    private UUID correlationId;

    public static RequestContext generateRequestContext(Map<String, String> headers){
        System.out.println(headers);
        UUID correlationId;
        String corrIdHeader = headers.get("x-correlation-id");
        correlationId = corrIdHeader==null?UUID.randomUUID():UUID.fromString(corrIdHeader);

        return RequestContext.builder()
                .correlationId(correlationId)
                .build();

    }
}
