package com.ecosystem.projectsservice.javaprojects.processes.process_control;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


@Getter
public class ChainProcess {




    public static enum ProcessStatus{
        WAITING, RUNNING, STOPPED, TERMINATED, PAUSED
    }

    private final AtomicReference<ProcessStatus> status = new AtomicReference<>(ProcessStatus.WAITING);

    // тот, кто останавливает или как то взаимодействует с процессом. может оставить сообщение
    private final AtomicReference<String> externalMessage = new AtomicReference<>(null);









    private AtomicReference<Instant> lastModified = new AtomicReference<>(Instant.now());


    private ExternalEventType processType;








    // если Running, но при
    private AtomicReference<String> currentStep = new AtomicReference<>(null);

    // универсальный идентификатор процесса

    private UUID correlationId;

    // используем interrupt для остановки текущего потока (аннотация @Duration).
    // исходя из этого, перед входом в каждый из методов происходит регистрация потока в state (это делаем под капотом)
    // нужно, чтобы методы, связанные с временем, выбрасывали interrupted exception - нужно дать понять пользователю, что это нужно сделать
    // актуально на время выполнения шага
    // null если никакой шаг не выполняется
    private AtomicReference<Thread> currentThread = new AtomicReference<>(null);

    // если шаг содержит в себе обращение к внешним системам машины через cmd - эти процессы должны быть уничтожены
    // таким образом, пользуясь аннотацией @Duration, пользователь обязан регистрировать процессы через stateManager
    // (как его заставить это сделать - отдельный вопрос)
    // null если никакой из шагов не выполняется
    private AtomicReference<List<Process>> currentNativeProcesses = new AtomicReference<>(null);

    public ChainProcess(UUID correlationId, ExternalEventType type){
        this.correlationId = correlationId;
        this.processType = type;
    }





    // заканчиваем шаг
    public void processCleanup(ProcessStatus nextStatus){
        lastModified.set(Instant.now());
        currentStep.set(null);
         // устанавливаем имя следующего ивента
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
        status.set(ProcessStatus.RUNNING);
        lastModified.set(Instant.now());
    }

    // регистрация процесса внутри метода шага - должно быть совершенно пользователем
    public void registerProcess(Process process){
        currentNativeProcesses.getAndUpdate(list -> {
            List<Process> processes = list==null?new ArrayList<>():list;
            processes.add(process);
            return processes;
        });
    }

    public void setStatus(ProcessStatus newStatus){
        lastModified.set(Instant.now());
        if (status.get()==ProcessStatus.STOPPED && newStatus!=ProcessStatus.TERMINATED) return; // если был остановлен, замена на другой статус  не происходит
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
        setStatus(ProcessStatus.TERMINATED);
    }







    // прежде всего, проставляем флаг running в false

    // может вызываться как снаружи, так и изнутри. Метод cleanup вызывается из цепочки
    public void stop(){
        lastModified.set(Instant.now());
        currentThread.getAndUpdate(thread -> {
            if (thread!=null) {
                thread.interrupt();
            }
            return thread;
        });
        status.set(ProcessStatus.STOPPED);
    }

    // пока что не реализуем, в проекте нет примеров таких процессов
    public void pause(){}



}
