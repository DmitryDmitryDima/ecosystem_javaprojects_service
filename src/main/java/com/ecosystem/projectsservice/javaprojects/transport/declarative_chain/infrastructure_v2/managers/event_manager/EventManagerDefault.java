package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.status_groups.DeliveryStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter.DeadLetter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter.DeadLetterChannel;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender.ChainManagerSender;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.outbox.OutboxModel;

import java.util.Optional;


// TODO при работе с event manager не забываем корректно проставлять delivery status
public class EventManagerDefault implements EventManager{

    // исходящий канал для расшифрованных ивентов
    private ChainManagerSender sender;

    // источник классов для расшифровки
    private EventRegistry registry;


    // mapper
    private MapperComponent mapperComponent;


    private ProcessAvatarStorage processAvatarStorage;

    private DeadLetterChannel deadLetterChannel;




    public EventManagerDefault(){}

    public EventManagerDefault(ChainManagerSender sender,
                               EventRegistry registry,
                               MapperComponent mapper,
                               ProcessAvatarStorage runtimeStorage,
                               DeadLetterChannel deadLetterChannel
                               ){
        this.sender = sender;
        this.registry = registry;
        this.mapperComponent = mapper;
        this.processAvatarStorage = runtimeStorage;
        this.deadLetterChannel = deadLetterChannel;
    }






    public ChainManagerSender getSender() {
        return sender;
    }

    public void setSender(ChainManagerSender sender) {
        this.sender = sender;
    }

    public EventRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(EventRegistry registry) {
        this.registry = registry;
    }

    public MapperComponent getMapperComponent() {
        return mapperComponent;
    }

    public void setMapperComponent(MapperComponent mapperComponent) {
        this.mapperComponent = mapperComponent;
    }


    public ProcessAvatarStorage getProcessRuntimeStorage() {
        return processAvatarStorage;
    }

    public void setProcessRuntimeStorage(ProcessAvatarStorage processAvatarStorage) {
        this.processAvatarStorage = processAvatarStorage;
    }

    public void setDeadLetterChannel(DeadLetterChannel deadLetterChannel) {
        this.deadLetterChannel = deadLetterChannel;
    }

    public DeadLetterChannel getDeadLetterChannel() {
        return deadLetterChannel;
    }



    private ChainEvent readPayload(OutboxModel model){




        // проверяем тип
        String type = model.getType();

        if (type == null){
            throw new EventManagerException("в прочитанной outbox" +
                    " модели отсутствует тип, чтение состояния процесса невозможно");

        }

        String payload = model.getPayload();

        if (payload == null){
            throw new EventManagerException("в прочитанной outbox модели не записан ивент," +
                    " чтение состояние я процесса невозможно");
        }

        Optional<Class<? extends ChainEvent>> clazzCheck
                = registry.getRegisteredClass(type);

        if (clazzCheck.isEmpty()){
            throw new EventManagerException("тип," +
                    " указанный в прочитанной outbox модели, не был зарегистрирован." +
                    " Чтение состояния процесса невозможно");
        }


        ChainEvent chainEvent = mapperComponent.read(payload, clazzCheck.get());

        chainEvent.setOutboxId(model.getOutboxUUID());



        return chainEvent;










    }



    // в данном случае аватар может отсутствовать, поэтому проверку не проводим

    @Override
    public ManagerResult workWithWaitingEvent(OutboxModel model) {


        try {

            ChainEvent chainEvent = readPayload(model);

            chainEvent.getProcessingInfo()
                    .setDeliveryStatus(DeliveryStatus.SUCCESS_READING);

            sender.send(chainEvent);






            return new ManagerResult();







        }

        // ошибка менеджера

        catch (Exception exception){

            return ManagerResult.exception(exception);


        }




    }



    @Override
    public ManagerResult workWithExpiredWaitingEvent(OutboxModel model) {

        try {
            ChainEvent chainEvent = readPayload(model);

            chainEvent.getProcessingInfo()
                    .setDeliveryStatus(DeliveryStatus.EXPIRED_READING);


            sender.send(chainEvent);

            return new ManagerResult();


        }

        catch (Exception e){

            return ManagerResult.exception(e);
        }


    }


    // проводится проверка на аватар

    // если аватара нет, то выставляется специфический delivery status



    @Override
    public ManagerResult workWithEverlastingProcessingEvent(OutboxModel model) {


        try {

            Optional<ProcessAvatar> avatarCheck = processAvatarStorage
                    .getAvatarById(model.getProcessUUID());


            // это означает, что процесс уже был однажды отправлен на компенсацию,
            // но processed callback не получил
            if (model.isCompensation()){

                // убиваем аватар, если он есть
                avatarCheck.ifPresent(ProcessAvatar::terminate);


                // генерируем dead letter

                DeadLetter deadLetter = new DeadLetter("Зависшая компенсация", model);

                deadLetterChannel.send(deadLetter);

                // провоцируем мгновенный dead letter в бд

                return ManagerResult.deadLetter();



            }


            if (avatarCheck.isPresent()){

                ProcessAvatar avatar = avatarCheck.get();






                // компенсация была вызвана, но зависла или не опубликовалась
                if (avatar.getStatus().get() == ProcessAvatarStatus.COMPENSATING ||
                        avatar.getStatus().get() == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_COMPENSATION){

                    // убиваем аватар, тем самым убивая зависшую компенсацию
                    avatar.terminate();

                    // генерируем dead letter

                    DeadLetter deadLetter = new DeadLetter("Зависшая или" +
                            " не опубликованная  компенсация", model);

                    deadLetterChannel.send(deadLetter);


                    // провоцируем мгновенный dead letter в бд

                    return ManagerResult.deadLetter();








                }


                ChainEvent chainEvent = readPayload(model);


                // шаг завершился, но не опубликовался. Отправляем в цепь для компенсации

                if (avatar.getStatus().get() == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_CRASH){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_CRASH);

                    // цепь должна спровоцировать компенсацию

                    sender.send(chainEvent);

                    // это компенсационный сценарий, пользователь сам принимает решение о ретрае
                    return ManagerResult.compensation();


                }

                else if (avatar.getStatus().get() == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STEP){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STEP);


                    sender.send(chainEvent);

                    return ManagerResult.compensation();
                }

                else if (avatar.getStatus().get() == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_FINAL_STEP){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STEP);


                    sender.send(chainEvent);

                    return ManagerResult.compensation();
                }




                else if (avatar.getStatus().get() == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STOP){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STOP);

                    sender.send(chainEvent);

                    return ManagerResult.compensation();
                }


                else {

                    // остальные статусы игнорируются
                    return new ManagerResult();
                }


            }


            // аватара нет - контекст процесса был потерян

            // компенсационная ситуация, поэтому делаем пометку в бд

            // не забываем, что аватар будет воскрешен и получит статус compensating

            else {



                ChainEvent chainEvent = readPayload(model);

                chainEvent.getProcessingInfo().setDeliveryStatus(DeliveryStatus
                        .EVERLASTING_STEP_MISSING_CONTEXT);

                sender.send(chainEvent);

                return ManagerResult.compensation();





            }






        }
        catch (Exception e){

            return ManagerResult.exception(e);

        }



    }


    // время выполнения текущего ивента просрочено.

    // ивенты данной группы уже имеют статус compensating в бд,
    // что означает, что внезапно оживший процесс не сможет вдруг продолжить цепь

    @Override
    public ManagerResult workWithExpiredProcessingEvent(OutboxModel model) {



        try {

            ChainEvent chainEvent = readPayload(model);

            Optional<ProcessAvatar> avatar = processAvatarStorage
                    .getAvatarById(chainEvent.getProcessId());

            if (avatar.isEmpty()){

                chainEvent.getProcessingInfo().setDeliveryStatus(DeliveryStatus
                        .EXPIRED_PROCESSING_MISSING_CONTEXT);
            }

            else {






                // шаг на самом деле завершился, но не смог опубликоваться
                // мутируем статус на аватаре в delivery status

                ProcessAvatarStatus avatarStatus = avatar.get().getStatus().get();

                // компенсационный шаг не смог корректно опубликоваться или завис - dead letter
                if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_COMPENSATION ||
                        avatarStatus == ProcessAvatarStatus.COMPENSATING){


                    // убиваем аватар, тем самым убивая зависшую компенсацию
                    avatar.get().terminate();

                    // генерируем dead letter

                    DeadLetter deadLetter = new DeadLetter("Зависшая или" +
                            " неопубликованная компенсация", model);

                    deadLetterChannel.send(deadLetter);


                    // провоцируем мгновенный dead letter в бд

                    return ManagerResult.deadLetter();

                }

                if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_CRASH){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_CRASH);
                }

                else if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STEP){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STEP);
                }

                else if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_FINAL_STEP){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_FINAL_STEP);
                }

                else if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STOP){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STOP);
                }




                else {
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.EXPIRED_PROCESSING_WITH_CONTEXT);
                }



            }


            sender.send(chainEvent);


            // всегда компенсационный сценарий
            return ManagerResult.compensation();





        }
        catch (Exception e){
            return ManagerResult.exception(e);
        }


    }

    // dead letter

    // убийство аватара


    // TODO тут, по идее, может бть уместен анализ содержимого аватара, если он есть
    @Override
    public ManagerResult workWithMissedExpiredProcessingEvent(OutboxModel model) {



        processAvatarStorage.getAvatarById(model.getProcessUUID())
                    .ifPresent(ProcessAvatar::terminate);




        deadLetterChannel.send(new DeadLetter("Processing ивент был прочитан несколько раз. " +
                "Возможно зависание. Сообщение из модели: "+model.getMessage(), model));


        return new ManagerResult();
    }

    @Override
    public ManagerResult workWithManagerCrashEvent(OutboxModel model) {


        processAvatarStorage.getAvatarById(model.getProcessUUID())
                    .ifPresent(ProcessAvatar::terminate);








        deadLetterChannel.send(new
                DeadLetter("невозможно расшифровать или отправить ивент, " +
                "сообщение из модели "+model.getMessage(),
                model));



        return new ManagerResult();
    }


    // наличие аватара не важно

    @Override
    public ManagerResult workWithExpiredWaitingForSignalEvent(OutboxModel model) {

        try {

            ChainEvent chainEvent = readPayload(model);

            chainEvent.getProcessingInfo()
                    .setDeliveryStatus(DeliveryStatus
                            .EXPIRED_WAITING_FOR_SIGNAL);

            sender.send(chainEvent);


            return ManagerResult.compensation();







        }

        // ошибка менеджера

        catch (Exception exception){


            return ManagerResult.exception(exception);


        }

    }
}
