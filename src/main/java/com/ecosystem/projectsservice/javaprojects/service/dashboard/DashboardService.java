package com.ecosystem.projectsservice.javaprojects.service.dashboard;


import com.ecosystem.projectsservice.javaprojects.dto.dashboard.AvatarDTO;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.IndexGroupDTO;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {


    @Autowired
    private ProcessAvatarStorage avatarStorage;




    public List<AvatarDTO> getAllAvatars(){

        return avatarStorage.getAll().stream().map(entity->{

            AvatarDTO avatarDTO = new AvatarDTO();

            avatarDTO.setCorrelationId(entity.getCorrelationId());
            avatarDTO.setStatus(entity.getStatus().get());
            avatarDTO.setCurrentStep(entity.getCurrentStep().get());

            return avatarDTO;


        }).toList();

    }


    public List<IndexGroupDTO> getAllIndexGroups(){


        return null;
    }



}
