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
    private UUID renderId;

    public static RequestContext generateRequestContext(Map<String, String> headers){
        System.out.println(headers);
        UUID correlationId;
        String corrIdHeader = headers.get("x-correlation-id");
        correlationId = corrIdHeader==null?UUID.randomUUID():UUID.fromString(corrIdHeader);

        String renderIdHeader = headers.get("x-render-id");


        return RequestContext.builder()
                .correlationId(correlationId)
                .renderId(renderIdHeader==null?null:UUID.fromString(renderIdHeader))
                .build();

    }
}
