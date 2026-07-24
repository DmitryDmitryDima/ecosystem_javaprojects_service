package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.registry.ChainEventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.output_actions.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter.DeadLetter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter.DeadLetterChannel;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModel;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelDefault;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;

import java.util.Optional;

// дефолтная реализация, чей контракт - публикация в репозиторий и проставление
// mark as processed для parent outbox

// TODO ДОБАВИТЬ ВЗАИМОДЕЙСТВИЯ С АВАТАРОМ ДЛЯ ВСЕХ СЦЕНАРИЕВ
public class OutputProcessorDefault implements OutputProcessor {


    private OutboxModelRepository repository;
    private MapperComponent mapper;



    private DeadLetterChannel deadLetterChannel;

    private ProcessAvatarStorage avatarStorage;


    public OutputProcessorDefault(OutboxModelRepository repository,
                                  MapperComponent mapper, ProcessAvatarStorage avatarStorage,
                                  DeadLetterChannel deadLetterChannel){
        this.repository = repository;
        this.mapper = mapper;
        this.deadLetterChannel = deadLetterChannel;
        this.avatarStorage = avatarStorage;
    }

    public OutputProcessorDefault(){}



    // todo используем дефолтную модель для аккумуляции данных
    // todo обработка происходит с опорой на Output Action







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



        return model;

    }

    // данный сценарий предполагает только сохранение нового ивента
    protected void chainInitOutputScenario(ChainOutput output,
                                         OutputMetadata<?> metadata){


        try {

            OutboxModel model = prepareNewEvent(output, metadata);

            repository.create(model);

        }

        catch (Exception e){
            throw new OutputProcessorException("Ошибка сценария публикации." +
                    " Не удалось запустить процесс: "+e.getMessage());
        }


    }


    // внимание - для компенсации используется конкретный метод репозитория
    protected void chainCompensationEndOutputScenario(ChainOutput output,
                                                      OutputMetadata<?> meta){



        try {

            repository.markAsProcessedForCompensation(output.getEvent().getOutboxId());

            // уничтожение аватара
            avatarStorage.getAvatarById(output.getEvent()
                            .getProcessId())
                    .ifPresent(ProcessAvatar::terminate);

        }
        catch (Exception e){

            // ошибка проставления коллбэка. Нужно уведомить аватар специальным статусом

            Optional<ProcessAvatar> avatarCheck = avatarStorage.getAvatarById(output.getEvent()
                    .getProcessId());

            // компенсация не может быть everlasting! Если аватара нет,
            // его обработают как обычный processing
            // поэтому не рассматриваем ситуацию, когда аватар недоступен
            avatarCheck.ifPresent(processAvatar
                    -> processAvatar.processCleanup(ProcessAvatarStatus.OUTPUT_ERROR,
                    output,
                    meta));



        }






    }

    // атомарный коллбэк плюс публикация нового ивента (компенсационного)
    protected void chainCrashOutputScenario(ChainOutput output,
                                            OutputMetadata<?> meta){


        try {


            repository.markPreviousAsProcessedAndCreateNewModel(output.getEvent().getOutboxId(),
                    prepareNewEvent(output, meta));


            // перевод аватара в статус crashed, его очистка от остатков прошлого процесса

            avatarStorage.getAvatarById(output.getEvent()
                            .getProcessId())
                    .ifPresent(processAvatar
                            -> processAvatar.processCleanup(ProcessAvatarStatus
                            .CRASHED, output, meta));





        }

        catch (Exception e){


            // ошибка публикации

            Optional<ProcessAvatar> avatarCheck = avatarStorage.getAvatarById(output.getEvent()
                    .getProcessId());

            if (avatarCheck.isEmpty()){

                // если шаг бесконечен, мы не можем проигнорировать ситуацию,
                // где публикация провалена, а также нет аватара
                if (meta.getExecutedStep().isEverlasting()){

                    DeadLetter deadLetter = new DeadLetter("Критическая ошибка" +
                            " - невозможно передать информацию об падении everlasting шага.",
                            output.getEvent().getOutboxId(),
                            output.getEvent().getProcessId());

                    deadLetterChannel.send(deadLetter);
                }

                // для шагов с фиксированным таймаутом выполнения политика предполагает,
                // что такой шаг будет обработан reader'ом в дальнейшем
                // (отсутствие аватара будет подмечено с помощью missing context)

            }

            else {
                avatarCheck.get().processCleanup(ProcessAvatarStatus
                        .OUTPUT_ERROR_AFTER_CRASH, output, meta);
            }



        }



    }

    // атомарный коллбэк + публикация нового ивента, особый статус для аватара.
    // Информация о stop проставлена в самом ивенте
    protected void chainStopOutputScenario(ChainOutput output, OutputMetadata<?> meta){



        try {

            // создание новой модели все равно проверяет processing на предыдущем ивенте
            repository.markPreviousAsProcessedAndCreateNewModel(output.getEvent().getOutboxId(),
                    prepareNewEvent(output, meta));


            // перевод аватара в статус stopped, его очистка от остатков прошлого процесса

            avatarStorage.getAvatarById(output.getEvent()
                            .getProcessId())
                    .ifPresent(processAvatar
                            -> processAvatar.processCleanup(ProcessAvatarStatus
                            .STOPPED, output, meta));





        }

        catch (Exception e){


            // ошибка публикации

            Optional<ProcessAvatar> avatarCheck = avatarStorage.getAvatarById(output.getEvent()
                    .getProcessId());

            if (avatarCheck.isEmpty()){

                // если шаг бесконечен, мы не можем проигнорировать ситуацию,
                // где публикация провалена, а также нет аватара
                if (meta.getExecutedStep().isEverlasting()){

                    DeadLetter deadLetter = new DeadLetter("Критическая ошибка" +
                            " - невозможно передать информацию об остановке everlasting шага.",
                            output.getEvent().getOutboxId(),
                            output.getEvent().getProcessId());

                    deadLetterChannel.send(deadLetter);
                }

                // для шагов с фиксированным таймаутом выполнения политика предполагает,
                // что такой шаг будет обработан reader'ом в дальнейшем
                // (отсутствие аватара будет подмечено с помощью missing context)

            }

            else {
                avatarCheck.get().processCleanup(ProcessAvatarStatus
                        .OUTPUT_ERROR_AFTER_STOP, output, meta);
            }



        }





    }


    // атомарный коллбэк плюс публикация нового ивента
    protected void chainStepOutputScenario(ChainOutput output, OutputMetadata<?> meta){


        try {


            repository.markPreviousAsProcessedAndCreateNewModel(output.getEvent().getOutboxId(),
                    prepareNewEvent(output, meta));


            // перевод аватара в статус waiting, его очистка от остатков прошлого процесса

            avatarStorage.getAvatarById(output.getEvent()
                            .getProcessId())
                    .ifPresent(processAvatar
                            -> processAvatar.processCleanup(ProcessAvatarStatus
                            .WAITING, output, meta));





        }
        catch (Exception e){


            // ошибка публикации

            Optional<ProcessAvatar> avatarCheck = avatarStorage.getAvatarById(output.getEvent()
                    .getProcessId());

            if (avatarCheck.isEmpty()){

                // если шаг бесконечен, мы не можем проигнорировать ситуацию,
                // где публикация провалена, а также нет аватара
                if (meta.getExecutedStep().isEverlasting()){

                    DeadLetter deadLetter = new DeadLetter("Критическая ошибка" +
                            " - невозможно передать информацию о завершении everlasting шага.",
                            output.getEvent().getOutboxId(),
                            output.getEvent().getProcessId());

                    deadLetterChannel.send(deadLetter);
                }

                // для шагов с фиксированным таймаутом выполнения политика предполагает,
                // что такой шаг будет обработан reader'ом в дальнейшем
                // (отсутствие аватара будет подмечено с помощью missing context)

            }

            else {
                avatarCheck.get().processCleanup(ProcessAvatarStatus.OUTPUT_ERROR, output, meta);
            }



        }


    }





    // внимание - последний шаг может быть бесконечным

    protected void chainEndOutputScenario(ChainOutput output,
                                          OutputMetadata<?> meta){

        try {

            repository.markAsProcessedForSuccessStep(output.getEvent().getOutboxId());

            // уничтожение аватара
            avatarStorage.getAvatarById(output.getEvent()
                    .getProcessId())
                    .ifPresent(ProcessAvatar::terminate);

        }
        catch (Exception e){

            // ошибка проставления коллбэка. Нужно уведомить аватар специальным статусом

            Optional<ProcessAvatar> avatarCheck = avatarStorage.getAvatarById(output.getEvent()
                    .getProcessId());

            if (avatarCheck.isEmpty()){

                // если шаг бесконечен, мы не можем проигнорировать ситуацию,
                // где публикация провалена, а также нет аватара
                if (meta.getExecutedStep().isEverlasting()){

                    DeadLetter deadLetter = new DeadLetter("Критическая ошибка" +
                            " - невозможно передать информацию о завершении everlasting шага," +
                            " являющего конечной точкой процесса.",
                            output.getEvent().getOutboxId(),
                            output.getEvent().getProcessId());

                    deadLetterChannel.send(deadLetter);
                }

                // для шагов с фиксированным таймаутом выполнения политика предполагает,
                // что такой шаг будет обработан reader'ом в дальнейшем
                // (отсутствие аватара будет подмечено с помощью missing context)

            }

            else {
                avatarCheck.get().processCleanup(ProcessAvatarStatus.OUTPUT_ERROR,
                        output, meta);
            }

        }
    }




    @Override
    public void output(ChainOutput output, OutputMetadata<?> metadata) {


        try {

            OutputAction action = metadata.getAction();

            if (action == null){
                throw new OutputProcessorException("Отсутствует тип действия," +
                        " проверьте конфигурацию цепи");
            }


            if (action instanceof ChainInit){


                chainInitOutputScenario(output, metadata);
            }

            else if (action instanceof ChainEnd){
                chainEndOutputScenario(output, metadata);
            }

            else if (action instanceof CompensationEnd){
                chainCompensationEndOutputScenario(output, metadata);
            }

            else if (action instanceof ChainStop){
                chainStopOutputScenario(output, metadata);
            }


            // не забываем проставить соответствующий статус в самом ивенте
            else if (action instanceof ChainCrash){

                chainCrashOutputScenario(output, metadata);


            }



            else {

                chainStepOutputScenario(output, metadata);




            }


        }

        catch (Exception e){
            throw new OutputProcessorException("output processing error. Reason: "+e.getMessage());
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
