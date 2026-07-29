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

            ManagerResult result = new ManagerResult();
            result.setException(exception);

            return result;


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

            ManagerResult result = new ManagerResult();
            result.setException(e);

            return result;
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
            // но коллбэк не получил
            if (model.isCompensation()){

                // убиваем аватар, если он есть
                avatarCheck.ifPresent(ProcessAvatar::terminate);


                // генерируем dead letter

                DeadLetter deadLetter = new DeadLetter("Зависшая компенсация", model);

                deadLetterChannel.send(deadLetter);

                // провоцируем мгновенный dead letter в бд

                ManagerResult result = new ManagerResult();

                result.setNeedDeadLetter(true);

                return result;



            }


            if (avatarCheck.isPresent()){

                ProcessAvatar avatar = avatarCheck.get();



                // компенсация была вызвана, но зависла
                if (avatar.getStatus().get() == ProcessAvatarStatus.COMPENSATING){

                    // убиваем аватар, тем самым убивая зависшую компенсацию
                    avatar.terminate();

                    // генерируем dead letter

                    DeadLetter deadLetter = new DeadLetter("Зависшая компенсация", model);

                    deadLetterChannel.send(deadLetter);


                    // провоцируем мгновенный dead letter в бд

                    ManagerResult result = new ManagerResult();

                    result.setNeedDeadLetter(true);

                    return result;








                }


                ChainEvent chainEvent = readPayload(model);


                // шаг завершился, но не опубликовался. Отправляем в цепь

                if (avatar.getStatus().get() == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_CRASH){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_CRASH);

                    // цепь должна попробовать снова опубликовать шаг, взяв готовый state из аватара
                    // если аватар упадет в момент попадания в цепь,
                    // то шаг попадет в новый цикл проверки
                    sender.send(chainEvent);

                    return new ManagerResult();


                }

                else if (avatar.getStatus().get() == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STEP){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STEP);
                    // цепь должна попробовать снова опубликовать шаг, взяв готовый state из аватара
                    // если аватар упадет в момент попадания в цепь,
                    // то шаг попадет в новый цикл проверки
                    sender.send(chainEvent);

                    return new ManagerResult();
                }

                else if (avatar.getStatus().get() == ProcessAvatarStatus.OUTPUT_ERROR_AFTER_STOP){
                    chainEvent.getProcessingInfo()
                            .setDeliveryStatus(DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STOP);
                    // цепь должна попробовать снова опубликовать шаг, взяв готовый state из аватара
                    // если аватар упадет в момент попадания в цепь,
                    // то шаг попадет в новый цикл проверки
                    sender.send(chainEvent);

                    return new ManagerResult();
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

                ManagerResult managerResult = new ManagerResult();
                managerResult.setWithCompensation(true);

                return managerResult;





            }






        }
        catch (Exception e){

            ManagerResult managerResult = new ManagerResult();
            managerResult.setException(e);
            return managerResult;

        }



    }


    // время выполнения текущего ивента просрочено.

    // todo нужно поразмыслить, имеет ли значение наличие/отсутствие аватара

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


            ManagerResult managerResult = new ManagerResult();

            managerResult.setWithCompensation(true);


            return managerResult;





        }
        catch (Exception e){
            ManagerResult managerResult = new ManagerResult();
            managerResult.setException(e);
            return managerResult;
        }


    }

    // dead letter

    // убийство аватара

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


            ManagerResult result = new ManagerResult();

            result.setWithCompensation(true);

            return result;







        }

        // ошибка менеджера

        catch (Exception exception){


            ManagerResult managerResult = new ManagerResult();
            managerResult.setException(exception);
            return managerResult;


        }

    }
}
