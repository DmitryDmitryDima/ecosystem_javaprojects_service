package com.ecosystem.projectsservice.javaprojects.service.dashboard;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelRepository;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.AvatarDTO;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.AvatarsWithIndexes;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.IndexGroupDTO;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.context_category.ProjectEventFromSystemContextCategory;
import com.ecosystem.projectsservice.javaprojects.external_messaging.test.TestData;
import com.ecosystem.projectsservice.javaprojects.external_messaging.test.TestEvent;
import com.ecosystem.projectsservice.javaprojects.external_messaging.test.TestModifiedChain;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.ProcessAvatarStorage;
import com.ecosystem.projectsservice.javaprojects.service.processes.test_processes.TestChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {


    @Autowired
    private ProcessAvatarStorage avatarStorage;

    @Autowired
    private OutboxModelRepository repo;





    @Autowired
    private TestChain testChain;

    @Autowired
    private TestModifiedChain modifiedChain;



    public void runTestButton(){


        /*

        //directoryAddTestChain.init(getChainEvent());


        if (avatarStorage.getAll().isEmpty()){
            var testEvent = new TestChainEvent();
            testEvent.setProcessId(UUID.randomUUID());

            testChain.init(testEvent);
        }


        else {
            avatarStorage.getAll().forEach(ProcessAvatar::stop);
        }

         */



        if (!avatarStorage.getAll().isEmpty()){

            avatarStorage.getAll().forEach(avatar -> {


                UUID processId = avatar.getCorrelationId();

                repo.receiveSignal(processId);



            });

            return;
        }
        UUID uuid = UUID.randomUUID();


        TestEvent testEvent = new TestEvent();
        testEvent.setProcessId(uuid);

        ProjectEventFromSystemContextCategory contextCategory
                = new ProjectEventFromSystemContextCategory();

        contextCategory.setCorrelationId(uuid);
        contextCategory.setProjectId(UUID.randomUUID());

        TestData data = new TestData();

        data.setData("some data");








        testEvent.setExternalContext(contextCategory);
        testEvent.setExternalData(data);

        modifiedChain.init(testEvent);














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
