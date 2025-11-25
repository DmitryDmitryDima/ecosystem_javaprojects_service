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


    public static SecurityContext generateContext(Map<String, String> requestHeaders){

        return SecurityContext
                .builder()
                .role(requestHeaders.get("role"))
                .uuid(UUID.fromString(requestHeaders.get("uuid")))
                .username(requestHeaders.get("username"))
                .build();
    }


}
