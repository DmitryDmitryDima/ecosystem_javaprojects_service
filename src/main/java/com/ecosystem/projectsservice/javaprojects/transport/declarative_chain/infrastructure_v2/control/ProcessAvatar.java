package com.ecosystem.projectsservice.javaprojects
        .transport.declarative_chain.infrastructure_v2.control;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.ChainOutput;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


// TODO ПЕРЕСМОТРЕТЬ ЖИЗНЕННЫЙ ЦИКЛ В соответствии с новой цепью
@Getter
public class ProcessAvatar {





    // универсальный идентификатор процесса
    private UUID correlationId;

    // индексы - для реализации поиска процесса по вторичным ключам
    private List<ProcessAvatarIndex> indexes = new ArrayList<>();

    // статус
    private final AtomicReference<ProcessAvatarStatus> status
            = new AtomicReference<>(ProcessAvatarStatus.WAITING);

    // тот, кто останавливает или как то взаимодействует с процессом. может оставить сообщение
    private final AtomicReference<String> externalMessage
            = new AtomicReference<>(null);


    private AtomicReference<Instant> lastModified = new AtomicReference<>(Instant.now());




    // если Running, но при
    private AtomicReference<String> currentStep
            = new AtomicReference<>(null);


    // используем interrupt для остановки текущего потока (аннотация @Duration).
    // исходя из этого, перед входом в каждый из методов происходит регистрация потока в state (это делаем под капотом)
    // нужно, чтобы методы, связанные с временем, выбрасывали interrupted exception - нужно дать понять пользователю, что это нужно сделать
    // актуально на время выполнения шага
    // null если никакой шаг не выполняется
    private AtomicReference<Thread> currentThread
            = new AtomicReference<>(null);

    // если шаг содержит в себе обращение к внешним системам машины через cmd - эти процессы должны быть уничтожены
    // таким образом, пользуясь аннотацией @Duration, пользователь обязан регистрировать процессы через stateManager
    // (как его заставить это сделать - отдельный вопрос)
    // null если никакой из шагов не выполняется
    private AtomicReference<List<Process>> currentNativeProcesses
            = new AtomicReference<>(null);




    // ПОЛЯ ДЛЯ РЕАЛИЗАЦИИ МЕХАНИЗМА РЕТРАЙ ПУБЛИКАЦИИ
    // ЕСЛИ СТАТУС OUTPUT_ERROR,
    // ТО МЕНЕДЖЕР ИМЕЕТ ПРАВО ПРОБРОСИТЬ РЕЗУЛЬТАТ ВЫПОЛНЕНИЯ ИЗ АВАТАРА В ЦЕПЬ
    // СОХРАНИВ КОНТЕКСТ ВЫПОЛНЕНИЯ

    // механизм настраивается пользователем

    private AtomicReference<ChainOutput> previousOutput;

    private AtomicReference<OutputMetadata<?>> previousOutputMetadata;




    public ProcessAvatar(UUID correlationId){
        this.correlationId = correlationId;
    }


    public ProcessAvatar(UUID correlationId, List<ProcessAvatarIndex> indexes){
        this.correlationId = correlationId;
        this.indexes = indexes;
    }


    public void addIndexes(List<ProcessAvatarIndex> indexes){
        this.indexes = indexes;
    }


    // заканчиваем шаг
    public void performActionsAndSetStatus(ProcessAvatarStatus nextStatus,
                                           ChainOutput output, OutputMetadata<?> metadata ){


        previousOutput.set(output);
        previousOutputMetadata.set(metadata);



        lastModified.set(Instant.now());
        currentStep.set(null);


        setStatus(nextStatus);
        currentNativeProcesses.getAndUpdate((processes -> {
            if (processes!=null){
                processes.forEach((process)->{
                    try {
                        process.destroyForcibly();
                    }
                    catch (Exception e){

                    }
                });

            }

            return null;
        }));

        currentThread.set(null);


    }

    public void stepOnStart(String step){
        currentStep.set(step);
        currentThread.set(Thread.currentThread());
        status.set(ProcessAvatarStatus.RUNNING);
        lastModified.set(Instant.now());
    }

    // регистрация процесса внутри метода шага - должно быть совершенно пользователем
    public void registerNativeProcess(Process process){
        currentNativeProcesses.getAndUpdate(list -> {
            List<Process> processes = list==null?new ArrayList<>():list;
            processes.add(process);
            return processes;
        });
    }

    public void setStatus(ProcessAvatarStatus newStatus){
        lastModified.set(Instant.now());
        if (status.get()== ProcessAvatarStatus.STOPPED
                && newStatus!= ProcessAvatarStatus.TERMINATED) return; // если был остановлен, замена на другой статус  не происходит
        status.set(newStatus);
    }


    public void setCurrentStep(String step){
        currentStep.set(step);
    }

    public void setCurrentThread(Thread thread){
        currentThread.set(thread);
    }

    public void setExternalMessage(String message){
        externalMessage.set(message);
    }
    public void terminate(){
        lastModified.set(Instant.now());
        setStatus(ProcessAvatarStatus.TERMINATED);
    }

    // прежде всего, проставляем флаг running в false

    // может вызываться как снаружи, так и изнутри. Метод cleanup вызывается из output processor
    public void stop(){
        lastModified.set(Instant.now());
        currentThread.getAndUpdate(thread -> {
            if (thread!=null) {
                thread.interrupt();
            }
            return thread;
        });
        status.set(ProcessAvatarStatus.STOPPED);
    }



    // мгновенное убийство аватара

    public void terminateInstantly(){


    }





}
