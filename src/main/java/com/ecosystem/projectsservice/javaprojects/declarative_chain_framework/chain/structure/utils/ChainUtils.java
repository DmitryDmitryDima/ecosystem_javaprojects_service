package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.utils;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.ChainTimeUnit;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.ChainDefaults;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step.ChainStep;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step.StepCountedTime;

import java.time.Instant;

public class ChainUtils {



    public static Long convertToMillis(Long value, ChainTimeUnit unit){
        if (value == null) return null;
        return switch (unit){
            case MS->value;
            case SEC -> value*1000;
            case MIN -> value*1000*60;
            case HOURS -> value*1000*60*60;
            case DAYS -> value*1000*60*60*24;
        };
    }





    // выставляем дефолтные значения для всех параметров времени

    public static StepCountedTime countDefaultTimes(){
        StepCountedTime time = new StepCountedTime();

        Long readExpirationPeriod = ChainUtils.convertToMillis(ChainDefaults
                        .DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS,
                ChainTimeUnit.SEC);

        Long duration = ChainUtils.convertToMillis(ChainDefaults
                        .DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS,
                ChainTimeUnit.SEC);

        Long readLockPeriod = 0L;

        Instant now = Instant.now();

        Instant readExpiration = Instant.now()
                .plusMillis(readExpirationPeriod);






        time.setLastUpdate(now);

        time.setDuration(duration);
        time.setLockUntil(now);
        time.setReadLockPeriod(readLockPeriod);
        time.setCurrentReadExpiration(readExpiration);
        time.setReadExpirationPeriod(readExpirationPeriod);



        return time;
    }


    public static StepCountedTime countTimeForNextStep(ChainStep nextStep){


        StepCountedTime time = new StepCountedTime();

        // duration первого шага
        // если everlasting, то значение остается null
        // в противном случае указывается время на основании
        // пользовательского значения, или дефолт
        Long duration = null;
        if (!nextStep.isEverlasting()){
            duration = ChainUtils.convertToMillis(nextStep.getTimeLimit(),
                    nextStep.getTimeLimitUnit());
        }


            /*
             расчет времени, до которого должен быть прочитан ивент, зависит от:
             - read lock аннотации
             - waiting for signal аннотации
             */


        Instant currentReadExpiration;

        Instant lockUntil = null;

        Long readExpirationPeriod
                = ChainUtils.convertToMillis(nextStep.getReadExpiration(),
                nextStep.getReadExpirationUnit());



        Long waitingForSignalPeriod = ChainUtils.convertToMillis(nextStep.getWaitingForSignal(),
                nextStep.getWaitingForSignalUnit());

        Long readLockPeriod = ChainUtils.convertToMillis(nextStep.getReadLock(),
                nextStep.getReadLockUnit());


        // проставляем дефолты для read lock period и read expiration period

        // если лока нет, выставляем 0
        if (readLockPeriod == null){
            readLockPeriod = 0L;
        }

        if (readExpirationPeriod == null){
            readExpirationPeriod = ChainUtils.convertToMillis(ChainDefaults
                            .DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS,
                    ChainTimeUnit.SEC);
        }


        Instant now = Instant.now();

        time.setLastUpdate(now);





        // для waiting for signal система будет использовать записанные значения периодов
        // для расчета read_expiration после активации цепи

        // исходя из этого, current read expiration время высчитывается исходя из данных в аннотации
        if (waitingForSignalPeriod!=null){
            currentReadExpiration = now.plusMillis(waitingForSignalPeriod);




        }

        // для обычного ивента read_expiration это now() + lock_period + read_expiration_period
        // lock_until фиксируется сразу
        else {



            lockUntil = now
                    .plusMillis(readLockPeriod);


            currentReadExpiration = lockUntil.plusMillis(readExpirationPeriod);

        }




        time.setDuration(duration);
        time.setLockUntil(lockUntil);
        time.setCurrentReadExpiration(currentReadExpiration);
        time.setReadExpirationPeriod(readExpirationPeriod);
        time.setReadLockPeriod(readLockPeriod);

        return time;



    }
}
