package com.ecosystem.projectsservice.javaprojects.service.dashboard;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.storage.TriggerStorage;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.ChainTrigger;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.PushStrategy;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.TriggerFeed;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.TriggerPhaseStrategy;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelRepository;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.AvatarDTO;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.AvatarsWithIndexes;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.IndexGroupDTO;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.context_category.ProjectEventFromSystemContextCategory;
import com.ecosystem.projectsservice.javaprojects.external_messaging.test.TestData;
import com.ecosystem.projectsservice.javaprojects.external_messaging.test.TestEvent;
import com.ecosystem.projectsservice.javaprojects.external_messaging.test.TestModifiedChain;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.structure.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.storage.ProcessAvatarStorage;
import com.ecosystem.projectsservice.javaprojects.service.processes.test_processes.TestChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {


    @Autowired
    private ProcessAvatarStorage avatarStorage;

    @Autowired
    private OutboxModelRepository repo;


    @Autowired
    private TriggerStorage storage;





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










        UUID genuuid = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

        try {
            storage.feedTrigger(new TriggerFeed(genuuid, "Hello","dima"));
        }

        catch (Exception e){
            e.printStackTrace();
        }


        /*

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

         */


        TriggerPhaseStrategy strategy = TriggerPhaseStrategy.constructStrategy()

                .addPhase((answers)->{
                    System.out.println("phase 1");
                    System.out.println(answers);
                    return false;
                }, 6_000)

                .addPhase((answers)->{
                    System.out.println("phase 2");
                    System.out.println(answers);
                    return false;
                }, 8_000)

                .getStrategy();


        ChainTrigger trigger = ChainTrigger

                .builder()
                .processId(genuuid)
                .expiration(Instant.now().plusSeconds(200))
                .pushStrategy(PushStrategy.WAITING_FOR_SIGNAL)
                .phaseStrategy(strategy)
                .reaction((answers)->{
                    System.out.println("reaction for "+answers);
                    return false;
                })
                .construct();


        storage.registerTrigger(trigger);



















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
