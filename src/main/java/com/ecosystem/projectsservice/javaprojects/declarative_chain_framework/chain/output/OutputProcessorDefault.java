package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.registry.ChainEventQualifier;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.output_actions.*;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.ProcessAvatarStatus;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModel;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelDefault;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelRepository;




public class OutputProcessorDefault implements OutputProcessor {


    private OutboxModelRepository repository;
    private MapperComponent mapper;





    public OutputProcessorDefault(OutboxModelRepository repository,
                                  MapperComponent mapper){
        this.repository = repository;
        this.mapper = mapper;


    }

    public OutputProcessorDefault(){}











    // готовим сущность для ее сохранения в базу данных
    protected OutboxModel prepareNewEvent(ChainOutput output,
                                        OutputMetadata<?> metadata){




        ChainEvent chainEvent = output.getEvent();

        String type
                = chainEvent.getClass()
                .getAnnotation(ChainEventQualifier.class).value();


        OutboxModelDefault model = new OutboxModelDefault();

        model.setProcessUUID(chainEvent.getProcessId());
        model.setStatus(output.getStatus());

        model.setType(type);
        model.setPayload(mapper.writeValueAsString(chainEvent));
        model.setLastUpdate(output.getLast_update());
        model.setReadExpiration(output.getReadExpiration());
        model.setPerformanceLimitTime(output.getPerformanceExpirationPeriod());

        model.setLockedUntil(output.getLockUntil());

        // параметры выставляются в случае, если статус waiting for signal
        model.setReadLockPeriod(output.getReadLockPeriod());
        model.setReadExpirationPeriod(output.getReadExpirationPeriod());






        return model;

    }

    // данный сценарий предполагает только сохранение нового ивента


    protected OutputResult chainInitOutputScenario(ChainOutput output,
                                         OutputMetadata<?> metadata,
                                           ProcessAvatar avatar){


        try {

            OutboxModel model = prepareNewEvent(output, metadata);

            repository.create(model);

            return OutputResult.success();

        }

        catch (Exception e){

            // мгновенное убийство
            avatar.terminate();

            return new OutputResult(false, e, "Ошибка сценария " +
                    "публикации init события:  "+e.getMessage());

        }


    }


    // внимание - для компенсации используется конкретный метод репозитория
    protected OutputResult chainCompensationEndOutputScenario(ChainOutput output,
                                                      OutputMetadata<?> meta,
                                                      ProcessAvatar avatar){



        try {

            System.out.println("processor - chain compensation end callback ");

            repository.markAsProcessedForCompensation(output.getEvent().getOutboxId());


            // terminate статус
            avatar.terminate();


            return OutputResult.success();



        }
        catch (Exception e){

            // ошибка проставления коллбэка. Нужно уведомить аватар специальным статусом
            avatar.setOutputStatus(ProcessAvatarStatus.OUTPUT_ERROR_AFTER_COMPENSATION,
                    output, meta
                    );


            return new OutputResult(false, e,
                    "Ошибка публикации при закрытии компенсации "
                    +e.getMessage());







        }






    }

    // атомарный коллбэк плюс публикация нового ивента (компенсационного)
    protected OutputResult chainCrashOutputScenario(ChainOutput output,
                                            OutputMetadata<?> meta,
                                            ProcessAvatar avatar){


        try {


            repository.markPreviousAsProcessedAndCreateNewModel(output.getEvent().getOutboxId(),
                    prepareNewEvent(output, meta));


            // перевод аватара в статус crashed, его очистка от остатков прошлого процесса

            avatar.setOutputStatus(ProcessAvatarStatus.CRASHED, output, meta);

            return OutputResult.success();








        }

        catch (Exception e){

            // уведомляем систему, что публикация не удалась
            avatar.setOutputStatus(ProcessAvatarStatus.OUTPUT_ERROR_AFTER_CRASH,
                    output, meta);


            return new OutputResult(false,
                    e, "ошибка публикации после сбоя цепи "+e.getMessage());





        }



    }

    // атомарный коллбэк + публикация нового ивента, особый статус для аватара.
    // Информация о stop проставлена в самом ивенте
    protected OutputResult chainStopOutputScenario(ChainOutput output, OutputMetadata<?> meta,
                                           ProcessAvatar avatar){



        try {

            // создание новой модели все равно проверяет processing на предыдущем ивенте
            repository.markPreviousAsProcessedAndCreateNewModel(output.getEvent().getOutboxId(),
                    prepareNewEvent(output, meta));


            // перевод аватара в статус stopped, его очистка от остатков прошлого процесса

            avatar.setOutputStatus(ProcessAvatarStatus.STOPPED, output, meta);

            return OutputResult.success();





        }

        catch (Exception e){


            // ошибка публикации

            avatar.setOutputStatus(ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STOP,
                    output, meta);

            return new OutputResult(false, e,
                    "ошибка публикации после остановки цепи "+e.getMessage());



        }





    }


    // атомарный коллбэк плюс публикация нового ивента
    protected OutputResult chainStepOutputScenario(ChainOutput output,
                                           OutputMetadata<?> meta, ProcessAvatar avatar){


        try {







            repository.markPreviousAsProcessedAndCreateNewModel(output.getEvent().getOutboxId(),
                    prepareNewEvent(output, meta));


            // перевод аватара в статус waiting, его очистка от остатков прошлого процесса

            avatar.setOutputStatus(ProcessAvatarStatus.WAITING, output, meta);

            return OutputResult.success();













        }
        catch (Exception e){

            e.printStackTrace();


            avatar.setOutputStatus(ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STEP,
                    output, meta);

            return new OutputResult(false, e, "ошибка публикации после выполнения шага "
                    +e.getMessage());



        }


    }





    // внимание - последний шаг может быть бесконечным

    protected OutputResult chainEndOutputScenario(ChainOutput output,
                                          OutputMetadata<?> meta, ProcessAvatar avatar){

        try {



            repository.markAsProcessedForSuccessStep(output.getEvent().getOutboxId());

            avatar.terminate();

            return OutputResult.success();






        }
        catch (Exception e){

            avatar.setOutputStatus(ProcessAvatarStatus.OUTPUT_ERROR_AFTER_FINAL_STEP,
                    output, meta);

            return new OutputResult(false,
                    e, "Ошибка коллбэка после завершения последнего шага "
                    +e.getMessage());

        }
    }




    @Override
    public OutputResult output(ChainOutput output,
                               OutputMetadata<?> metadata,
                               ProcessAvatar avatar) {




        OutputAction action = metadata.getAction();

        if (action == null){
            return new OutputResult(false, null, "Не указан тип действия");
        }


        if (action instanceof ChainInit){


            return chainInitOutputScenario(output, metadata, avatar);
        }

        else if (action instanceof ChainEnd){
            return chainEndOutputScenario(output, metadata, avatar);
        }

        else if (action instanceof CompensationEnd){
            return chainCompensationEndOutputScenario(output, metadata, avatar);
        }

        else if (action instanceof ChainStop) {
            return chainStopOutputScenario(output, metadata, avatar);
        }


        // не забываем проставить соответствующий статус в самом ивенте
        else if (action instanceof ChainCrash){

            return chainCrashOutputScenario(output, metadata, avatar);


        }



        else {

            return chainStepOutputScenario(output, metadata, avatar);




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
