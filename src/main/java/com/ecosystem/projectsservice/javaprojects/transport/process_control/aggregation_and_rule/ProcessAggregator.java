package com.ecosystem.projectsservice.javaprojects.transport.process_control.aggregation_and_rule;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessIndex;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


// сервис для агрегации существующих в системе процессов]
// способен выполнять поиск по correlation id, а также по id связанной с процессом сущности
@Service
public class ProcessAggregator {




    // все процессы
    private Map<UUID, ChainProcess> allProcesses = new HashMap<>();


    // пользовательские индексы

    // мы используем имя индекса, как ключ, и вторичный ключ, как ключ к внутренней таблице
    // допускается, что на один ключ может быть несколько процессов
    private Map<String, Map<String, List<ChainProcess>>> indexes = new HashMap<>();






    private final ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();

    private final Lock readLock = globalLock.readLock();
    private final Lock writeLock = globalLock.writeLock();




    // регистрируем процесс, при этом реализую прописанные пользователем индексы, если они есть
    public void registerChainProcess(ChainProcess chainProcess){
        writeLock.lock();


        try {
            // idempotency guard
            if (allProcesses.containsKey(chainProcess.getCorrelationId()))
                throw new IllegalStateException("Process already registered");
            allProcesses.put(chainProcess.getCorrelationId(), chainProcess);

            // читаем индексы

            List<ProcessIndex> userIndexes = chainProcess.getIndexes();

            for (var index:userIndexes){

                // извлекаем структуру, ассоциированную с заданным ключом
                Map<String, List<ChainProcess>> indexStructure = indexes.get(index.getKey());

                // если структуры нет, создаем новую
                if (indexStructure == null){
                    indexStructure = new HashMap<>();
                    indexes.put(index.getName(),indexStructure);
                }

                // извлекаем список, ассоциированный со вторичным ключом
                List<ChainProcess> processes
                        = indexStructure.computeIfAbsent(index.getKey(), k -> new ArrayList<>());

                // если процессов нет, создаем список и вставляем в структуру

                // добавляем процесс
                processes.add(chainProcess);


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



    // TODO скоро изменится

    // атомарная операция чтения и (опционально) создания процесса
    public ChainProcess getOrRestoreChainProcessByCorrelationId(UUID correlationId,
                                                                ChainProcess toRestore,
                                                                Runnable associationsRestore){
        writeLock.lock();

        try {
            if (allProcesses.containsKey(correlationId)) {
                ChainProcess chainProcess = allProcesses.get(correlationId);
                chainProcess.getCurrentThread().set(Thread.currentThread());
                return chainProcess;
            }
            else {
                allProcesses.put(correlationId, toRestore);
                // восстанавливаем пользовательские ассоциации
                if (associationsRestore!=null){
                    associationsRestore.run();
                }

                return toRestore;
            }
        }
        finally {
            writeLock.unlock();
        }
    }



}
