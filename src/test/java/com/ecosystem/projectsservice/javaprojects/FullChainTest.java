package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.test_processes.DirectoryAddTestChain;
import com.ecosystem.projectsservice.javaprojects.service.processes.test_processes.DirectoryAddTestEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class FullChainTest {

    @Autowired
    private DirectoryAddTestChain directoryAddTestChain;



    @Test
    public void start(){

        directoryAddTestChain.init(getChainEvent());

        while (true){

        }
    }


    private DirectoryAddTestEvent getChainEvent(){





        DirectoryAddTestEvent testEvent = new DirectoryAddTestEvent();

        testEvent.setMessage("Hello i am test event");

        testEvent.setProcessId(UUID.fromString("019f7b9e-8712-7f01-b217-5bba7d96290a"));

        ProjectEventFromUserContext externalContext = new ProjectEventFromUserContext();

        externalContext.setUsername("user");
        externalContext.setUserUUID(UUID.randomUUID());
        externalContext.setRenderId(UUID.randomUUID());
        externalContext.setCorrelationId(testEvent.getProcessId());





        testEvent.setExternalContext(externalContext );


        DirectoryAddExternalData data = new DirectoryAddExternalData(UUID.randomUUID(),
                "new_folder", UUID.randomUUID());



        testEvent.setExternalData(data);



        return testEvent;






    }
}
