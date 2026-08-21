package com.ecosystem.projectsservice.javaprojects.dto.dashboard;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.ProcessAvatarStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvatarDTO {

    private UUID correlationId;

    private String currentStep;

    private ProcessAvatarStatus status;







}
