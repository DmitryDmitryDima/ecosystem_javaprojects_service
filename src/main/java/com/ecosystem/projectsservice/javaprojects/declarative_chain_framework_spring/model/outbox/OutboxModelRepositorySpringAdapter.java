package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.model.outbox;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModel;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelRepository;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxRepositoryException;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxStatus;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.model.idempotency.IdempotencyModelJpaEntity;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.model.idempotency.IdempotencyModelJpaRepository;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class OutboxModelRepositorySpringAdapter implements OutboxModelRepository {


    private static final long DEFAULT_LOCK_UNTIL_PERIOD_IN_SECONDS_FOR_PROCESSING_STATUS_READERS = 10;


    private OutboxModelJpaRepository outboxModelJpaRepository;




    private TransactionTemplate transaction;

    private IdempotencyModelJpaRepository idempotencyModelJpaRepository;


    public OutboxModelRepositorySpringAdapter(OutboxModelJpaRepository outboxModelJpaRepository,
                                              IdempotencyModelJpaRepository idempotencyModelJpaRepository,
                                              TransactionTemplate transaction) {
        this.outboxModelJpaRepository = outboxModelJpaRepository;
        this.transaction = transaction;
        this.idempotencyModelJpaRepository = idempotencyModelJpaRepository;
    }


    // создание новой записи
    // адаптер самостоятельно обязан предоставить дефолтный механизм идемпотентности
    // мы используем реестр таблицу, регистрирующую uuid
    @Override
    public void create(OutboxModel model) {

        try {


            // запись в реестр
            IdempotencyModelJpaEntity registryEntity = IdempotencyModelJpaEntity.builder()
                    .processId(model.getProcessUUID())
                    .build();




            // используя интерфейс, производим маппинг
            OutboxModelJpaEntity entity = OutboxModelJpaEntity.builder()


                    .processUUID(model.getProcessUUID())
                    .status(model.getStatus())
                    .type(model.getType())
                    .payload(model.getPayload())
                    .lastUpdate(model.getLastUpdate())
                    .readExpiration(model.getReadExpiration())
                    .performanceLimitTime(model.getPerformanceLimitTime())
                    .allReadVersion(0L)
                    .allReadProcessingVersion(0L)
                    .message(model.getMessage())

                    .lockedUntil(model.getLockedUntil())
                    .readExpirationPeriod(model.getReadExpirationPeriod())
                    .readLockPeriod(model.getReadLockPeriod())


                    .build();

            // создаем запись в реестре, в случае успеха - сохраняем outbox model
            transaction.execute(status -> {


                // в случае,
                // если uuid процесса уже фигурировал, процесс не будет зарегистрирован
                idempotencyModelJpaRepository
                        .save(registryEntity);



                outboxModelJpaRepository.save(entity);

                return null;
            });






        } catch (Exception e) {
            throw
                    new OutboxRepositoryException("Не удалось создать outbox модель, причина: "
                            + e.getMessage());
        }

    }


    // условия коллбэка - текущий статус - processing, compensation = false

    @Override
    public void markPreviousAsProcessedAndCreateNewModel(UUID previous, OutboxModel model) {




        // используя интерфейс, производим маппинг
        OutboxModelJpaEntity newEntity = OutboxModelJpaEntity.builder()


                .processUUID(model.getProcessUUID())
                .status(model.getStatus())
                .type(model.getType())
                .payload(model.getPayload())
                .lastUpdate(model.getLastUpdate())
                .readExpiration(model.getReadExpiration())
                .performanceLimitTime(model.getPerformanceLimitTime())
                .allReadVersion(0L)
                .allReadProcessingVersion(0L)
                .message(model.getMessage())

                .lockedUntil(model.getLockedUntil())
                .readExpirationPeriod(model.getReadExpirationPeriod())
                .readLockPeriod(model.getReadLockPeriod())

                .build();

        try {

            transaction.execute(status -> {


                Optional<OutboxModelJpaEntity> prevCheck = outboxModelJpaRepository
                        .findByUUIDForUpdate(previous);

                // если присутствует предыдущий ивент, он обязан соответствовать некоторым условиям
                if (prevCheck.isPresent()) {

                    var entity = prevCheck.get();

                    if (entity.isCompensation()) {
                        throw new IllegalStateException("ошибка коллбэка - ивент помечен, как компенсируемый");
                    }

                    if (entity.getStatus() != OutboxStatus.PROCESSING) {
                        throw new IllegalStateException("ошибка коллбэка - ивент имеет статус " + entity.getStatus());


                    }

                    entity.setLastUpdate(Instant.now());
                    entity.setAllReadVersion(entity.getAllReadVersion() + 1);

                    entity.setStatus(OutboxStatus.PROCESSED);


                    // публикуем новый ивент

                    outboxModelJpaRepository.save(newEntity);
                }


                // todo размышления - можно ли двигать цепь, если предыдущий ивент отсутствует
                // так или иначе в случае повторения той или иной ошибки зависший processing улетит в dead letter


                return null;

            });

        } catch (Exception e) {
            throw new OutboxRepositoryException("ошибка создания нового шага. Причина: " + e.getMessage());
        }
    }

    // todo каким должен быть такой запрос?
    /*
    должен ли он гарантировать, что предыдущий статус - processing, и compensation = false
    Так мы защищаем от ситуации,
     когда успешная цепь пытается продолжиться после входа в компенсационный режим
     */

    @Override
    public void markAsProcessedForSuccessStep(UUID id) {


        try {


            transaction.execute(status -> {


                Optional<OutboxModelJpaEntity> prevCheck = outboxModelJpaRepository
                        .findByUUIDForUpdate(id);

                // если присутствует предыдущий ивент, он обязан соответствовать некоторым условиям
                if (prevCheck.isPresent()) {

                    var entity = prevCheck.get();

                    if (entity.isCompensation()) {
                        throw new IllegalStateException("ошибка коллбэка - ивент помечен, как компенсируемый");
                    }

                    if (entity.getStatus() != OutboxStatus.PROCESSING) {
                        throw new IllegalStateException("ошибка коллбэка - ивент имеет статус " + entity.getStatus());


                    }

                    entity.setLastUpdate(Instant.now());
                    entity.setAllReadVersion(entity.getAllReadVersion() + 1);

                    entity.setStatus(OutboxStatus.PROCESSED);

                }


                return null;
            });


        } catch (Exception e) {
            throw
                    new OutboxRepositoryException("Ошибка проставления коллбэка для успешно выполненного шага "
                            + e.getMessage());
        }


    }


    // единственное условие - processing
    @Override
    public void markAsProcessedForCompensation(UUID id) {


        try {


            transaction.execute(status -> {


                Optional<OutboxModelJpaEntity> prevCheck = outboxModelJpaRepository
                        .findByUUIDForUpdate(id);

                // если присутствует предыдущий ивент, он обязан соответствовать некоторым условиям
                if (prevCheck.isPresent()) {

                    var entity = prevCheck.get();


                    if (entity.getStatus() != OutboxStatus.PROCESSING) {
                        throw new IllegalStateException("ошибка коллбэка " +
                                "- ивент имеет статус " + entity.getStatus());
                    }

                    entity.setLastUpdate(Instant.now());

                    entity.setAllReadVersion(entity.getAllReadVersion() + 1);


                    entity.setStatus(OutboxStatus.PROCESSED);

                }


                return null;
            });


        } catch (Exception e) {
            throw new OutboxRepositoryException("ошибка коллбэка для компенсационного шага. Причина: "
                    + e.getMessage());
        }

    }


    // todo нужно ли изменение all read?
    @Override
    public void markAsCompensating(UUID id) {


        try {


            transaction.execute(status -> {


                Optional<OutboxModelJpaEntity> prevCheck = outboxModelJpaRepository
                        .findByUUIDForUpdate(id);


                prevCheck.ifPresent(entity -> entity.setCompensation(true));


                return null;
            });


        } catch (Exception e) {
            throw new OutboxRepositoryException("Не удалось пометить шаг как компенсационный: "
                    + e.getMessage());
        }


    }

    @Override
    public void changeStatusForGivenAllReadVersion(UUID uuid, OutboxStatus toStatus, Long forAllReadVersion) {
        try {

            transaction.execute(status -> {


                Optional<OutboxModelJpaEntity> check = outboxModelJpaRepository.findByUUIDForUpdate(uuid);

                if (check.isPresent()) {

                    var entity = check.get();

                    if (!entity.getAllReadVersion().equals(forAllReadVersion)) {
                        throw new IllegalStateException("несовпадение версий");
                    }

                    entity.setStatus(toStatus);
                    entity.setLastUpdate(Instant.now());
                    entity.setAllReadVersion(entity.getAllReadVersion() + 1);
                } else {
                    throw new IllegalStateException("Сущности нет");
                }


                return null;
            });

        } catch (Exception e) {
            throw new OutboxRepositoryException("ошибка изменения статуса. Причина " + e.getMessage());
        }
    }

    @Override
    public void changeStatusAndMessageForGivenAllReadVersion(UUID uuid,
                                                             OutboxStatus toStatus,
                                                             String message,
                                                             Long forAllReadVersion) {
        try {

            transaction.execute(status -> {


                Optional<OutboxModelJpaEntity> check = outboxModelJpaRepository.findByUUIDForUpdate(uuid);

                if (check.isPresent()) {

                    var entity = check.get();

                    if (!entity.getAllReadVersion().equals(forAllReadVersion)) {
                        throw new IllegalStateException("несовпадение версий");
                    }

                    entity.setStatus(toStatus);
                    entity.setMessage(message);
                    entity.setLastUpdate(Instant.now());
                    entity.setAllReadVersion(entity.getAllReadVersion() + 1);
                } else {
                    throw new IllegalStateException("Сущности нет");
                }


                return null;
            });

        } catch (Exception e) {
            throw new OutboxRepositoryException("ошибка изменения статуса. Причина " + e.getMessage());
        }
    }

    @Override
    public void receiveSignal(UUID processUUID) {

        // все делается атомарно

        // поиск (pessimistic write) - process uuid + waiting_for_signal status

        // вычисляем новый read_expiration и lock_until на основании read_expiration_period и read_lock_period



        //

    }


    // обновляем readVersion, атомарно меняем статус на processing


    // update - учитываем lock_until поле
    @Override
    public List<? extends OutboxModel> readActualWaitingEvents() {

        try {
            return
                    transaction.execute(status -> {
                        /*
                                List<OutboxModelJpaEntity> jpaEntities
                                        = outboxModelJpaRepository
                                        .readAllEntitiesByStatusWhereReadExpirationNotReached(OutboxStatus
                                                .WAITING);

                         */

                        List<OutboxModelJpaEntity> jpaEntities = outboxModelJpaRepository
                                .readAllWaitingEntitiesWhereReadExpirationNotReachedAndReadLockFree();



                        // при чтении статус меняется на processing,
                                // обновляется last update,
                                // а также происходит обновление readVersion
                        jpaEntities.forEach(outboxModelJpaEntity -> {
                                    outboxModelJpaEntity.setLastUpdate(Instant.now());
                                    outboxModelJpaEntity.setAllReadVersion(outboxModelJpaEntity
                                            .getAllReadVersion() + 1);
                                    outboxModelJpaEntity.setStatus(OutboxStatus.PROCESSING);



                                });

                                return jpaEntities;
                            }
                    );
        } catch (Exception e) {
            throw new
                    OutboxRepositoryException("Не удалось получить актуальные Waiting записи. Причина: " + e.getMessage());
        }
    }

    @Override
    public List<? extends OutboxModel> readActualWaitingEvents(Long batchSize) {
        return List.of();
    }


    @Override
    public List<? extends OutboxModel> readEverlastingProcessingEvents(Long batchSize) {
        return List.of();
    }


    // чтение происходит с обновлением allRead и allProcessing read
    @Override
    public List<? extends OutboxModel> readEverlastingProcessingEvents() {


        try {


            return transaction.execute(status -> {

                List<OutboxModelJpaEntity> everlastingSteps = outboxModelJpaRepository
                        .readEverlastingSteps(OutboxStatus.PROCESSING);


                everlastingSteps.forEach(step -> {

                    // атомарно обновляем счетчик при чтении
                    step.setAllReadProcessingVersion(step.getAllReadProcessingVersion() + 1);
                    step.setAllReadVersion(step.getAllReadVersion());

                });


                return everlastingSteps;
            });


        } catch (Exception e) {

            throw new OutboxRepositoryException("ошибка чтения активных бесконечных шагов: " + e.getMessage());
        }


    }


    // dead letter
    @Override
    public List<? extends OutboxModel> readMissedExpiredProcessingEvents() {


        try {


            return transaction.execute(status -> {


                List<OutboxModelJpaEntity> entities = outboxModelJpaRepository
                        .readMissedEventsExpiredByPerformance();


                entities.forEach(entity -> {
                    entity.setAllReadProcessingVersion(entity.getAllReadProcessingVersion()+1);
                    entity.setAllReadVersion(entity.getAllReadVersion()+1);
                    entity.setLastUpdate(Instant.now());
                    entity.setStatus(OutboxStatus.DEAD_LETTER);
                });

                return entities;
            });
        }

        catch (Exception e){

            throw new OutboxRepositoryException("Ошибка чтения многократно зависших шагов. "
                    +e.getMessage());
        }



    }

    @Override
    public List<? extends OutboxModel> readMissedExpiredProcessingEvents(Long batchSize) {
        return List.of();
    }


    // ивент просрочился по времени выполнения, получает значение processing чтения = 1.
    // Более медленный обработчик должен читать ивенты, который не вышли из этого состояния зависания


    @Override
    public List<? extends OutboxModel> readExpiredProcessingEvents() {


        try {


            return transaction.execute(status -> {


                List<OutboxModelJpaEntity> entities = outboxModelJpaRepository
                        .readEventsExpiredByPerformance();


                entities.forEach(entity -> {
                    entity.setAllReadProcessingVersion(entity.getAllReadProcessingVersion()+1);
                    entity.setAllReadVersion(entity.getAllReadVersion()+1);

                    // time lock, чтобы missing processing поток не прочитал ивент во время компенсации
                    entity.setLockedUntil(Instant.now()
                            .plusSeconds(DEFAULT_LOCK_UNTIL_PERIOD_IN_SECONDS_FOR_PROCESSING_STATUS_READERS));

                    // явный компенсационный сценарий помечается атомарно
                    entity.setCompensation(true);
                });

                return entities;
            });
        }

        catch (Exception e){

            throw new OutboxRepositoryException("Ошибка чтения зависших шагов. "+e.getMessage());
        }
    }

    @Override
    public List<? extends OutboxModel> readExpiredProcessingEvents(Long batchSize) {
        return List.of();
    }





    @Override
    public List<? extends OutboxModel> readExpiredWaitingEvents() {

        try {
            return
                    transaction.execute(status -> {

                        List<OutboxModelJpaEntity> jpaEntities = outboxModelJpaRepository
                                .readAllEntitiesWithReadExpirationReached(OutboxStatus.WAITING);


                        // при чтении статус меняется на processing,
                        // обновляется last update,
                        // а также происходит обновление readVersion
                        jpaEntities.forEach(outboxModelJpaEntity -> {
                            outboxModelJpaEntity.setLastUpdate(Instant.now());
                            outboxModelJpaEntity.setAllReadVersion(outboxModelJpaEntity.getAllReadVersion()+1);
                            outboxModelJpaEntity.setStatus(OutboxStatus.PROCESSING);

                            // явный компенсационный сценарий помечается атомарно
                            outboxModelJpaEntity.setCompensation(true);

                        });

                        return jpaEntities;}
                    );
        }

        catch (Exception e){
            throw new
                    OutboxRepositoryException("Не удалось получить просроченные Waiting записи. Причина: " +
                    ""+e.getMessage());
        }


    }

    @Override
    public List<? extends OutboxModel> readExpiredWaitingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readExpiredWaitingForSignalEvents() {


        try {
            return
                    transaction.execute(status -> {

                        List<OutboxModelJpaEntity> jpaEntities = outboxModelJpaRepository
                                .readAllEntitiesWithReadExpirationReached(OutboxStatus.WAITING_FOR_SIGNAL);


                        // при чтении статус меняется на processing,
                        // обновляется last update,
                        // а также происходит обновление readVersion
                        jpaEntities.forEach(outboxModelJpaEntity -> {
                            outboxModelJpaEntity.setLastUpdate(Instant.now());
                            outboxModelJpaEntity.setAllReadVersion(outboxModelJpaEntity.getAllReadVersion()+1);
                            outboxModelJpaEntity.setStatus(OutboxStatus.PROCESSING);


                            // явный компенсационный сценарий помечается атомарно
                            outboxModelJpaEntity.setCompensation(true);

                        });

                        return jpaEntities;}
                    );
        }

        catch (Exception e){
            throw new
                    OutboxRepositoryException("Не удалось получить просроченные" +
                    " Waiting for External записи. Причина: " +
                    ""+e.getMessage());
        }

    }

    @Override
    public List<? extends OutboxModel> readExpiredWaitingForSignalEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readManagerCrashEvents() {

        try {

            return transaction.execute(status -> {



                List<OutboxModelJpaEntity> events = outboxModelJpaRepository.readByStatus(OutboxStatus.MANAGER_CRASH);

                // счетчик + статус
                events.forEach(event->{
                    event.setAllReadVersion(event.getAllReadVersion()+1);
                    event.setStatus(OutboxStatus.DEAD_LETTER);
                    event.setLastUpdate(Instant.now());



                });


                return events;
            });

        }

        catch (Exception e){
            throw new OutboxRepositoryException("Ошибка чтения managed_crashed шагов. "+e.getMessage());
        }




    }

    @Override
    public List<? extends OutboxModel> readManagerCrashEvents(Long batchSize) {
        return List.of();
    }
}
