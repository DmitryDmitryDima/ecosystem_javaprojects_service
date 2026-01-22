package com.ecosystem.projectsservice.javaprojects.processes.process_control;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;



public abstract class ChainProcess {


    // данный флаг нужен для того, чтобы в случае прихода запоздавшего ивента (или если процесс на паузе) система знала, что его не нужно выполнять

    private AtomicBoolean running;

    // если не null, то шаг выполняется прямо сейчас. если Duration таймер (связан с именем шага) заканчивается,
    // и видит, что currentStep = duration timer step, то выбрасывается ошибка о превышении времени выполнения шага

    // принцип работы @WaitingFor - в конце метода, выполняемого перед аннотируемым этой аннотацией методом,
    // мы запускаемым таймер с названием аннотированного метода. Если таймер заканчивается и видит, что current step все еще null и статус running
    // то выбрасывается исключение

    // если в цепочке несколько waiting for, то должен быть механизм замены таймера, чтобы предыдущий таймер случайно не сработал




    // ситуация, где ивент прилетел, но увидел, что state вообще отсутствует - ивент так же не обрабатывается
    private AtomicReference<String> currentStep;

    // универсальный идентификатор процесса
    private UUID correlationId;

    // используем interrupt для остановки текущего потока (аннотация @Duration).
    // исходя из этого, перед входом в каждый из методов происходит регистрация потока в state (это делаем под капотом)
    // нужно, чтобы методы, связанные с временем, выбрасывали interrupted exception - нужно дать понять пользователю, что это нужно сделать
    private AtomicReference<Thread> currentThread;

    // если шаг содержит в себе обращение к внешним системам машины через cmd - эти процессы должны быть уничтожены
    // таким образом, пользуясь аннотацией @Duration, пользователь обязан регистрировать процессы через stateManager
    // (как его заставить это сделать - отдельный вопрос)
    private AtomicReference<List<Process>> currentProcesses = new AtomicReference<>(null);

    public ChainProcess(UUID correlationId){
        this.correlationId = correlationId;
    }



    // заканчиваем шаг
    public void endStep(){
        currentStep.set(null);

        currentProcesses.getAndUpdate((processes -> {
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

    public void startStep(String step){
        currentStep.set(step);
        currentThread.set(Thread.currentThread());
    }

    // регистрация процесса внутри метода шага - должно быть совершенно пользователем
    public void registerProcess(Process process){
        currentProcesses.getAndUpdate(list -> {
            List<Process> processes = list==null?new ArrayList<>():list;
            processes.add(process);
            return processes;
        });
    }



    // прежде всего, проставляем флаг running в false

    // может вызываться как снаружи, так и изнутри
    public void stop(){}

    // пока что не реализуем, в проекте нет примеров таких процессов
    public void pause(){}










}
