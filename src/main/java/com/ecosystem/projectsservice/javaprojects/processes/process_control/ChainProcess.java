package com.ecosystem.projectsservice.javaprojects.processes.process_control;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


@Getter
@Setter
public abstract class ChainProcess {

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


    private final AtomicReference<String> waitingForEvent = new AtomicReference<>(null);



    // данный флаг нужен для того, чтобы в случае прихода запоздавшего ивента (или если процесс на паузе) система знала, что его не нужно выполнять
    // также на него может ориентироваться сам шаг
    // todo - вопрос. сразу ли удалять state из агрегатора, или какое то время держать его с running false??
    private AtomicBoolean running;


    private AtomicReference<Instant> lastModified;


    private ExternalEventType processType;

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
    private AtomicReference<List<Process>> currentNativeProcesses = new AtomicReference<>(null);

    public ChainProcess(UUID correlationId, ExternalEventType type, String firstStep){
        this.correlationId = correlationId;
        this.processType = type;
        this.waitingForEvent.set(firstStep);
    }



    // заканчиваем шаг
    public void endStep(){
        currentStep.set(null);

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

    public void startStep(String step){
        currentStep.set(step);
        currentThread.set(Thread.currentThread());
        status.set(ProcessStatus.RUNNING);
    }

    // регистрация процесса внутри метода шага - должно быть совершенно пользователем
    public void registerProcess(Process process){
        currentNativeProcesses.getAndUpdate(list -> {
            List<Process> processes = list==null?new ArrayList<>():list;
            processes.add(process);
            return processes;
        });
    }



    // прежде всего, проставляем флаг running в false

    // может вызываться как снаружи, так и изнутри
    public void stop(){

    }

    // пока что не реализуем, в проекте нет примеров таких процессов
    public void pause(){}










}
