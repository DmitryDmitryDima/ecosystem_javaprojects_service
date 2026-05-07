package com.ecosystem.projectsservice.javaprojects.dto.projects.state;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Autosave {

    private UUID fileId;
    private UUID projectId;
    private UUID projectRoot;
    private UUID projectOwner;
    private String content;


    private SecurityContext securityContext;
    private RequestContext requestContext;




}
