package com.ecosystem.projectsservice.javaprojects.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityContext {

    private UUID uuid;

    private String username;

    private String role;

    private UUID targetUUID;




    public static SecurityContext generateContext(Map<String, String> requestHeaders){

        String targetUUID = requestHeaders.get("targetuuid");


        return SecurityContext
                .builder()
                .role(requestHeaders.get("role"))
                .uuid(UUID.fromString(requestHeaders.get("uuid")))
                .username(requestHeaders.get("username"))
                .targetUUID(targetUUID==null?null:UUID.fromString(targetUUID))
                .build();
    }


}
