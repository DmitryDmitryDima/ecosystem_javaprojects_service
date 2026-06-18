package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control;


import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// бывший process aggregator
public class ProcessRuntimeStorageImpl implements ProcessRuntimeStorage {


    // все процессы
    private final Map<UUID, DeclarativeChainProcess> allProcesses = new HashMap<>();


    // пользовательские индексы

    // мы используем имя индекса, как ключ, и вторичный ключ, как ключ к внутренней таблице
    // допускается, что на один ключ может быть несколько процессов
    private Map<String, Map<String, List<DeclarativeChainProcess>>> indexes = new HashMap<>();


    private final ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();

    private final Lock readLock = globalLock.readLock();
    private final Lock writeLock = globalLock.writeLock();



    private void createIndexes(DeclarativeChainProcess process){
        // читаем индексы

        List<ProcessIndex> userIndexes = process.getIndexes();

        for (var index:userIndexes){

            // извлекаем структуру, ассоциированную с заданным ключом
            Map<String, List<DeclarativeChainProcess>> indexStructure = indexes.get(index.getKey());

            // если структуры нет, создаем новую
            if (indexStructure == null){
                indexStructure = new HashMap<>();
                indexes.put(index.getName(),indexStructure);
            }

            // извлекаем список, ассоциированный со вторичным ключом
            List<DeclarativeChainProcess> processes
                    = indexStructure.computeIfAbsent(index.getKey(), k -> new ArrayList<>());

            // если процессов нет, создаем список и вставляем в структуру

            // добавляем процесс
            processes.add(process);


        }
    }



    // регистрируем процесс, при этом реализую прописанные пользователем индексы, если они есть
    public void registerChainProcess(DeclarativeChainProcess chainProcess){
        writeLock.lock();


        try {
            // idempotency guard
            if (allProcesses.containsKey(chainProcess.getCorrelationId()))
                throw new IllegalStateException("Process already registered");
            allProcesses.put(chainProcess.getCorrelationId(), chainProcess);

            // создаем индексы
            createIndexes(chainProcess);




        }
        finally {
            writeLock.unlock();
        }
    }



    public Optional<DeclarativeChainProcess> getChainProcessById(UUID correlationId){
        readLock.lock();

        try {
            return Optional.ofNullable(allProcesses.get(correlationId));
        }
        finally {
            readLock.unlock();
        }
    }

    public DeclarativeChainProcess getOrRestore(UUID correlationId,
                                                DeclarativeChainProcess toRestore){
        writeLock.lock();

        try {
            if (allProcesses.containsKey(correlationId)) {
                DeclarativeChainProcess chainProcess = allProcesses.get(correlationId);
                chainProcess.getCurrentThread().set(Thread.currentThread());
                return chainProcess;
            }
            else {
                allProcesses.put(correlationId, toRestore);

                // восстанавливаем индексы
                createIndexes(toRestore);




                return toRestore;
            }
        }
        finally {
            writeLock.unlock();
        }
    }




}
