package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ChainsTest {


    @Test
    public void timerTest() throws InterruptedException {



        try (ScheduledExecutorService service = Executors.newScheduledThreadPool(2)) {
            service.schedule(()->{
                System.out.println("task in 500 ms");
            }, 500, TimeUnit.MILLISECONDS);

            service.schedule(()->{
                System.out.println("task in 3000 ms");
            }, 3000, TimeUnit.MILLISECONDS);
        }

        System.out.println("after all");
        Thread.ofVirtual().start(
                ()->{
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("task 1 2000 ms");
                }
        );

        Thread.ofVirtual().start(
                ()->{
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("task 2 8000 ms");
                }
        );

        Thread.ofVirtual().start(
                ()->{
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("task 3 15000ms");
                }
        );

        Thread.sleep(10000);
        System.out.println("10000 ms main");



        Thread.sleep(10000);
        System.out.println("20000 ms main");


    }


}
