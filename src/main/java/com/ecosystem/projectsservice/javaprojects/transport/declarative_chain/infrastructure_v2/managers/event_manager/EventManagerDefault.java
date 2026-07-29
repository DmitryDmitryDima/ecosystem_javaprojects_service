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
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModel;

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

        return mapperComponent.read(payload,
                clazzCheck.get());





    }



    // в данном случае аватар может отсутствовать, поэтому проверку не проводим

    @Override
    public ManagementResult workWithWaitingEvent(OutboxModel model) {


        try {

            ChainEvent chainEvent = readPayload(model);

            chainEvent.getProcessingInfo()
                    .setDeliveryStatus(DeliveryStatus.SUCCESS_READING);

            sender.send(chainEvent);

            return new ManagementResult(true, null);







        }

        // ошибка менеджера

        catch (Exception exception){
            return new ManagementResult(false, exception);


        }




    }


    // проверка на аватар не проводится
    @Override
    public ManagementResult workWithExpiredWaitingEvent(OutboxModel model) {

        try {
            ChainEvent chainEvent = readPayload(model);

            chainEvent.getProcessingInfo()
                    .setDeliveryStatus(DeliveryStatus.EXPIRED_READING);


            sender.send(chainEvent);

            return new ManagementResult(true, true);


        }

        catch (Exception e){

            return new ManagementResult(false, e);
        }


    }


    // проводится проверка на аватар

    // если аватара нет, то выставляется специфический delivery status

    // если аватар есть, то ничего не делаем
    // - шаг бесконечен, пока его не убьют вручную и есть аватар

    @Override
    public ManagementResult workWithEverlastingProcessingEvent(OutboxModel model) {


        try {


            // если модель помечена, как отправленная на компенсацию, но не получила коллбэк







            ChainEvent chainEvent = readPayload(model);

            Optional<ProcessAvatar> avatar = processAvatarStorage
                    .getAvatarById(chainEvent.getProcessId());


            if (avatar.isEmpty()){
                chainEvent.getProcessingInfo().setDeliveryStatus(DeliveryStatus
                        .EVERLASTING_STEP_MISSING_CONTEXT);

                sender.send(chainEvent);

                // уведомляем о компенсации
                return new ManagementResult(true, true);
            }

            // если аватар есть и он не output_error - не трогаем процесс

            // шаг на самом деле завершился, но не смог опубликоваться
            // мутируем статус на аватаре в delivery status

            ProcessAvatarStatus avatarStatus = avatar.get().getStatus().get();

            if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_CRASH){
                chainEvent.getProcessingInfo()
                        .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_CRASH);

                // цепь должна попробовать снова опубликовать шаг, взяв готовый state из аватара
                // если аватар упадет в момент попадания в цепь,
                // то шаг попадет в новый цикл проверки
                sender.send(chainEvent);


            }

            else if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STEP){
                chainEvent.getProcessingInfo()
                        .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STEP);
                // цепь должна попробовать снова опубликовать шаг, взяв готовый state из аватара
                // если аватар упадет в момент попадания в цепь,
                // то шаг попадет в новый цикл проверки
                sender.send(chainEvent);
            }

            else if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STOP){
                chainEvent.getProcessingInfo()
                        .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STOP);
                // цепь должна попробовать снова опубликовать шаг, взяв готовый state из аватара
                // если аватар упадет в момент попадания в цепь,
                // то шаг попадет в новый цикл проверки
                sender.send(chainEvent);
            }









            return new ManagementResult(true, null);





        }
        catch (Exception e){
            return new ManagementResult(false, e);
        }



    }


    // время выполнения текущего ивента просрочено.

    // todo нужно поразмыслить, имеет ли значение наличие/отсутствие аватара

    @Override
    public ManagementResult workWithExpiredProcessingEvent(OutboxModel model) {



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

                if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_CRASH){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_CRASH);
                }

                else if (avatarStatus == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STEP){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STEP);
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


            return new ManagementResult(true, true);





        }
        catch (Exception e){
            return new ManagementResult(false, e);
        }


    }

    // dead letter

    // убийство аватара

    @Override
    public ManagementResult workWithMissedExpiredProcessingEvent(OutboxModel model) {


        try {
            processAvatarStorage.getAvatarById(model.getProcessUUID())
                    .ifPresent(ProcessAvatar::terminateInstantly);
        }
        catch (Exception e){



        }


        deadLetterChannel.send(new DeadLetter("Processing ивент был прочитан несколько раз. " +
                "Возможно зависание. Сообщение из модели: "+model.getMessage(), model));


        return new ManagementResult(true, null);
    }

    @Override
    public ManagementResult workWithManagerCrashEvent(OutboxModel model) {

        try {
            processAvatarStorage.getAvatarById(model.getProcessUUID())
                    .ifPresent(ProcessAvatar::terminateInstantly);
        }
        catch (Exception e){



        }


        deadLetterChannel.send(new
                DeadLetter("невозможно расшифровать или отправить ивент, " +
                "сообщение из модели "+model.getMessage(),
                model));



        return new ManagementResult(true, null);
    }


    // наличие аватара не важно

    @Override
    public ManagementResult workWithExpiredWaitingForSignalEvent(OutboxModel model) {

        try {

            ChainEvent chainEvent = readPayload(model);

            chainEvent.getProcessingInfo()
                    .setDeliveryStatus(DeliveryStatus
                            .EXPIRED_WAITING_FOR_SIGNAL);

            sender.send(chainEvent);

            return new ManagementResult(true, true);







        }

        // ошибка менеджера

        catch (Exception exception){
            return new ManagementResult(false, exception);


        }

    }
}
