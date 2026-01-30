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

    /* WAITING - процесс не выполняет шаг, но при этом активен
         - ждет ивента для его запуска
         - имя ожидаемого шага указывается в поле waitingForEvent, если приходит другой шаг - он в игноре
         - Работает в комбинации с аннотацией waiting for, если таковая имеется
       RUNNING - выполняется какой-то из шагов
         - работает в комбинации с current step полем
         - если процесс долгий, пользователь должен ориентироваться на while(status.get()==Running) - таким образом реализуется остановка процесса

       PAUSED - пока пропускаем этот фукнционал

       STOPPED - процесс остановлен во время выполнения. Это означает, что необходимо провести cleanup и действия, указанные пользователем при остановке
         - Если прилетает ивент и мы видим, что процесс Stopped - ивент не выполняется
         - Если выполняется шаг и меняется флаг - происходит остановка (прерывание шага), далее cleanup
       TERMINATED - процесс закончен, больше не актуален
         - В программе этот флаг проставляется, когда заканчивается end step


      Edge cases - их рассмотрение так же зависит от того. как мы работаем с зависшими в processing outbox ивентами.
      Что, если правильнее хранить параметры времени в outbox, сканируя ивенты и принимая решение? звучит довольно громоздко, и не лишает необходимости в state
      Примеры edge cases:
         - Ивент пришел из внешней системы, он должен быть записан в outbox, но процесса нет в агрегаторе - по умолчанию это означает, что он не актуален - игнор
         (возможно существуют супер долгие процессы, когда ответ от какого то сервиса может прийти через очень долгое время,
         когда jvm уже успела запуститься - если необходимость в таких процессах возникнет, мне придется создавать persisted state
         вместо агрегатора в памяти. Возможно persisted копия может задаваться опционально пользователем, тогда ивент будет проверять не только aggregator
         - ивент прочитан из outbox, но процесса нет - восстанавливаем процесс. В рамках системы любой waiting outbox рассматривается, как
         необходимый к выполнению



     */


    public static enum ProcessStatus{
        WAITING, RUNNING, STOPPED, TERMINATED, PAUSED
    }

    private final AtomicReference<ProcessStatus> status = new AtomicReference<>(ProcessStatus.WAITING);









    private AtomicReference<Instant> lastModified = new AtomicReference<>(Instant.now());


    private ExternalEventType processType;

    // если не null, то шаг выполняется прямо сейчас. если Duration таймер (связан с именем шага) заканчивается,
    // и видит, что currentStep = duration timer step, то выбрасывается ошибка о превышении времени выполнения шага

    // принцип работы @WaitingFor - в конце метода, выполняемого перед аннотируемым этой аннотацией методом,
    // мы запускаемым таймер с названием аннотированного метода. Если таймер заканчивается и видит, что current step все еще null и статус running
    // то выбрасывается исключение

    // если в цепочке несколько waiting for, то должен быть механизм замены таймера, чтобы предыдущий таймер случайно не сработал




    // current step = null & waiting = состояние вне выполнения шага -> смотрим на waiting for

    // если Running, но при
    private AtomicReference<String> currentStep;

    // универсальный идентификатор процесса

    private UUID correlationId;

    // используем interrupt для остановки текущего потока (аннотация @Duration).
    // исходя из этого, перед входом в каждый из методов происходит регистрация потока в state (это делаем под капотом)
    // нужно, чтобы методы, связанные с временем, выбрасывали interrupted exception - нужно дать понять пользователю, что это нужно сделать
    // актуально на время выполнения шага
    // null если никакой шаг не выполняется
    private AtomicReference<Thread> currentThread;

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
        status.set(nextStatus);
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
        status.set(newStatus);
    }

    public void setCurrentStep(String step){
        currentStep.set(step);
    }

    public void setCurrentThread(Thread thread){
        currentThread.set(thread);
    }
    public void terminate(){
        lastModified.set(Instant.now());
        status.set(ProcessStatus.TERMINATED);
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
