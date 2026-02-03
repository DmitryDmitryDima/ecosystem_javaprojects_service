package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Timer;
import java.util.TimerTask;

@SpringBootTest
public class ChainsTest {


    @Test
    public void timerTest() throws InterruptedException {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("inside timer");
            }
        }, 3000);

        System.out.println("main thread");
        Thread.sleep(10000);
    }


}
