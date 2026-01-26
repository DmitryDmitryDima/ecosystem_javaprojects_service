package com.ecosystem.projectsservice.javaprojects.processes.process_control;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


// сервис для агрегации существующих в системе процессов]
// способен выполнять поиск по correlation id, а также по id связанной с процессом сущности
@Service
public class ProcessAggregator {




    // все процессы
    private Map<UUID, ChainProcess> allProcesses = new HashMap<>();


    // ассоциация по id проекта
    private Map<Long, List<ChainProcess>> projectProcesses = new HashMap<>();

    private final ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();

    private final Lock readLock = globalLock.readLock();
    private final Lock writeLock = globalLock.writeLock();


    public void registerChainProcess(ChainProcess chainProcess){
        writeLock.lock();
        try {
            allProcesses.putIfAbsent(chainProcess.getCorrelationId(), chainProcess);

            // если процесс относится к проекту - вставляем в список процессов
            if (chainProcess instanceof ProjectAssociatedProcess projectAssociatedProcess){
                projectProcesses.compute(projectAssociatedProcess.getProjectId(), (k,v)->{
                    if (v==null){
                        v = new ArrayList<>(List.of(chainProcess));
                    }
                    else {
                        v.add(chainProcess);
                    }
                    return v;
                });
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    public ChainProcess getChainProcessByCorrelationId(UUID correlationId){
        readLock.lock();

        try {
            return allProcesses.get(correlationId);
        }
        finally {
            readLock.unlock();
        }
    }










}
