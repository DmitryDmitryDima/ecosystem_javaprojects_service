package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.config;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessor;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessorDefault;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStorageImpl;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter.DeadLetterChannel;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter.DeadLetterChannelApplicationPublisher;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.EventManager;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.EventManagerDefault;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry.EventRegistryDefault;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader.OutboxReader;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader.OutboxReaderDefault;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender.ChainManagerSender;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender.ChainManagerSenderSpringApplicationPublisherAdapter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelJpaRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepositorySpringAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
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
    public ProcessAvatarStorage runtimeStorage(){
        return new ProcessAvatarStorageImpl();
    }


    @Bean
    @ConditionalOnMissingBean
    public OutputProcessor outputProcessor(OutboxModelRepository repository,
                                           MapperComponent mapper,
                                           DeadLetterChannel deadLetterChannel,
                                           ProcessAvatarStorage avatarStorage){

        return new OutputProcessorDefault(repository, mapper, avatarStorage, deadLetterChannel );

    }


    @Bean
    @ConditionalOnMissingBean
    public OutboxModelRepository repository(OutboxModelJpaRepository jpaRepository,
                                            TransactionTemplate transactionTemplate){


        return new OutboxModelRepositorySpringAdapter(jpaRepository, transactionTemplate);
    }


    @Bean
    @ConditionalOnMissingBean
    public OutboxReader outboxReader(OutboxModelRepository repository,
                                     EventManager eventManager){


        return new OutboxReaderDefault(repository, eventManager);
    }


    @Bean
    @ConditionalOnMissingBean
    public EventManager eventManager(EventRegistry registry,
                                     ProcessAvatarStorage runtimeStorage,
                                     MapperComponent mapperComponent,
                                     ChainManagerSender sender,
                                     DeadLetterChannel deadLetterChannel){


        return new EventManagerDefault(sender,
                registry,
                mapperComponent,
                runtimeStorage, deadLetterChannel);

    }

    @Bean
    @ConditionalOnMissingBean
    public DeadLetterChannel deadLetterChannel(ApplicationEventPublisher publisher){
        return new DeadLetterChannelApplicationPublisher(publisher);
    }






}
