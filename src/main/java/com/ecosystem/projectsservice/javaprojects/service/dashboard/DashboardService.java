package com.ecosystem.projectsservice.javaprojects.service.dashboard;


import com.ecosystem.projectsservice.javaprojects.dto.dashboard.AvatarDTO;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.AvatarsWithIndexes;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.IndexGroupDTO;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddExternalData;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.DirectoryAddTestChain;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.DirectoryAddTestEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.TestChain;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.TestChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {


    @Autowired
    private ProcessAvatarStorage avatarStorage;


    @Autowired
    private DirectoryAddTestChain directoryAddTestChain;


    @Autowired
    private TestChain testChain;



    public void runTestButton(){

        //directoryAddTestChain.init(getChainEvent());


        var testEvent = new TestChainEvent();
        testEvent.setProcessId(UUID.randomUUID());

        testChain.init(testEvent);






    }



    private DirectoryAddTestEvent getChainEvent(){

        DirectoryAddTestEvent testEvent = new DirectoryAddTestEvent();

        testEvent.setMessage("Hello i am test event");

        testEvent.setProcessId(UUID.randomUUID());

        ProjectEventFromUserContext externalContext = new ProjectEventFromUserContext();

        externalContext.setUsername("user");
        externalContext.setUserUUID(UUID.randomUUID());
        externalContext.setRenderId(UUID.randomUUID());
        externalContext.setCorrelationId(testEvent.getProcessId());
        externalContext.setProjectId(UUID.randomUUID());


        testEvent.setExternalContext(externalContext );


        DirectoryAddExternalData data = new DirectoryAddExternalData(UUID.randomUUID(),
                "new_folder", UUID.randomUUID());



        testEvent.setExternalData(data);
        return testEvent;


    }




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


        Map<String, Map<String, List<ProcessAvatar>>> currentIndexStructure = avatarStorage
                .getIndexesStructure();




        return currentIndexStructure.entrySet().stream().map(entry->{

            IndexGroupDTO indexGroup = new IndexGroupDTO();

            indexGroup.setName(entry.getKey());

            // функция для key, функция для value
            indexGroup.setBuckets(entry.getValue().entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    innerEntry->
                            innerEntry.getValue().stream().map(ProcessAvatar::getCorrelationId).toList())));



            return indexGroup;

        }).toList();



    }

    public AvatarsWithIndexes getAvatarsAndIndexes(){


        AvatarsWithIndexes dto = new AvatarsWithIndexes();

        dto.setAvatars(getAllAvatars());
        dto.setIndexes(getAllIndexGroups());

        return dto;
    }



}
