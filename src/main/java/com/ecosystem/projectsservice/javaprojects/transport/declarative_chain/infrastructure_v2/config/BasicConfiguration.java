package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.config;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessor;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessorDefault;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorageImpl;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry.EventRegistryDefault;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender.ChainManagerSender;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender.ChainManagerSenderSpringApplicationPublisherAdapter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class BasicConfiguration {



    @Bean
    @ConditionalOnMissingBean
    public EventRegistry eventRegistry(){


        return new EventRegistryDefault();
    }


    @Bean
    @ConditionalOnMissingBean
    public MapperComponent mapper(ObjectMapper mapper){

        return new MapperSpringAdapter(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChainManagerSender sender(ApplicationEventPublisher springPub){
        return new ChainManagerSenderSpringApplicationPublisherAdapter(springPub);
    }


    @Bean
    @ConditionalOnMissingBean
    public ProcessRuntimeStorage runtimeStorage(){
        return new ProcessRuntimeStorageImpl();
    }


    @Bean
    @ConditionalOnMissingBean
    public OutputProcessor outputProcessor(OutboxModelRepository repository,
                                           MapperComponent mapper){

        return new OutputProcessorDefault(repository, mapper);

    }



}
