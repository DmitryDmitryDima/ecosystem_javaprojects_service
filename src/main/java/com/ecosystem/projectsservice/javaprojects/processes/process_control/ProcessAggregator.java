package com.ecosystem.projectsservice.javaprojects.processes.process_control;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.exceptions.ChainInitiationException;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveEvent;
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
            // idempotency guard
            if (allProcesses.containsKey(chainProcess.getCorrelationId())) throw new IllegalStateException("Process already registered");
            allProcesses.put(chainProcess.getCorrelationId(), chainProcess);


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
