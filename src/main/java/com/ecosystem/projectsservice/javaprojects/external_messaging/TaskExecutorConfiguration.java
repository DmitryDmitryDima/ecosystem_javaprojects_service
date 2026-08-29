package com.ecosystem.projectsservice.javaprojects.external_messaging;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class  TaskExecutorConfiguration {

    // быстрые операции, пример - удаление проекта
    @Bean(name = "chainExecutor")
    public TaskExecutor defaultExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("default-");
        return executor;
    }


    // для удобства выделим отдельного исполнителя для запущенных проектов
    @Bean(name = "projectExecutor")
    public TaskExecutor threadPoolTaskExecutor() {

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(30);
        taskExecutor.setMaxPoolSize(40);
        taskExecutor.setQueueCapacity(10);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(30);
        taskExecutor.setThreadNamePrefix("project-exec-");
        taskExecutor.initialize();

        return taskExecutor;
    }

    @Bean(name="virtualThreadFactory")
    public Executor virtualThreadsFactory(){

        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
