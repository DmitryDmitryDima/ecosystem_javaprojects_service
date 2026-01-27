package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ChainProcess;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class ChainStateTests {



    @Test
    public void testChainState() throws InterruptedException {

        ProjectAssociatedProcess process = new ProjectAssociatedProcess(UUID.randomUUID(),
                ExternalEventType.JAVA_PROJECT_CREATION_FROM_TEMPLATE,
                1L,
                1L);

        process.getStatus().set(ChainProcess.ProcessStatus.RUNNING);

        Thread thread = new Thread(()->{
            while (process.getStatus().get()== ChainProcess.ProcessStatus.RUNNING && !Thread.currentThread().isInterrupted()){
                try {
                    System.out.println(Thread.currentThread().isInterrupted());
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("stopped externally");
        });
        thread.start();

        Thread.sleep(10000);

        process.getStatus().set(ChainProcess.ProcessStatus.STOPPED);
        thread.interrupt();

        Thread.sleep(5000);



    }



}
