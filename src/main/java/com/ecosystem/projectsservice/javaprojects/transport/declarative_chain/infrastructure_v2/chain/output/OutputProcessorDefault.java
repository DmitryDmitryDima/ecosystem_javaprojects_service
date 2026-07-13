package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.registry.ChainEventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.output_actions.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelDefault;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;

import java.util.UUID;

// дефолтная реализация, чей контракт - публикация в репозиторий и проставление
// mark as processed для parent outbox
public class OutputProcessorDefault implements OutputProcessor {


    private OutboxModelRepository repository;
    private MapperComponent mapper;


    public OutputProcessorDefault(OutboxModelRepository repository, MapperComponent mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    public OutputProcessorDefault(){}



    // todo используем дефолтную модель для аккумуляции данных
    // todo обработка происходит с опорой на Output Action






    private void publishNewEvent(ChainOutput output,
                                 OutputMetadata<?> metadata){

        try {


            ChainEvent chainEvent = output.getEvent();

            String type
                    = chainEvent.getClass().getAnnotation(ChainEventQualifier.class).value();


            OutboxModelDefault model = new OutboxModelDefault();

            model.setProcessUUID(chainEvent.getProcessId());
            model.setStatus(output.getStatus());
            model.setType(type);
            model.setPayload(mapper.writeValueAsString(chainEvent));
            model.setLastUpdate(output.getLast_update());
            model.setReadExpiration(output.getReadExpiration());
            model.setPerformanceLimitTime(output.getPerformanceExpirationPeriod());




            repository.save(model);







        }
        catch (Exception e){
            throw new OutputProcessorException("ошибка публикации outbox сообщения: "+e.getMessage(),
                    "PUBLISHING_ERROR");
        }

    }

    @Override
    public void publish(ChainOutput output, OutputMetadata<?> metadata) {


        try {

            OutputAction action = metadata.getAction();

            if (action == null){
                throw new OutputProcessorException("missing action merker, check origin",
                        "OUTPUT_ERROR");
            }


            if (action instanceof ChainInit){
                publishNewEvent(output, metadata);
            }

            else if (action instanceof ChainEnd){
                repository.markAsProcessed(output.getEvent().getOutboxId());
            }


            else {
                publishNewEvent(output, metadata);
                repository.markAsProcessed(output.getEvent().getOutboxId());
            }


        }

        catch (Exception e){
            throw new OutputProcessorException("output processing error. Reason: "+e.getMessage(),
                    "OUTPUT_ERROR");
        }










    }







    public void setRepository(OutboxModelRepository repository) {
        this.repository = repository;
    }

    public OutboxModelRepository getRepository(){
        return this.repository;
    }

    public void setMapper(MapperComponent mapper){
        this.mapper = mapper;
    }

    public MapperComponent getMapper(){
        return mapper;
    }
}
