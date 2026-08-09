package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control;


import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

// бывший process aggregator
public class ProcessAvatarStorageImpl implements ProcessAvatarStorage {


    // все процессы
    private final Map<UUID, ProcessAvatar> allProcesses = new HashMap<>();


    // пользовательские индексы

    // мы используем имя индекса, как ключ, и вторичный ключ, как ключ к внутренней таблице
    // допускается, что на один ключ может быть несколько процессов
    private Map<String, Map<String, List<ProcessAvatar>>> indexes = new HashMap<>();


    private final ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();

    private final Lock readLock = globalLock.readLock();
    private final Lock writeLock = globalLock.writeLock();



    private void createIndexes(ProcessAvatar process){
        // читаем индексы

        List<ProcessAvatarIndex> userIndexes = process.getIndexes();

        for (var index:userIndexes){

            // извлекаем структуру, ассоциированную с заданным именем, или создаем новую
            // пример имени индекса - projects
            Map<String, List<ProcessAvatar>> indexStructure
                    = indexes.computeIfAbsent(index.getName(), k -> new HashMap<>());



            // извлекаем список, ассоциированный с вторичным ключом (например, это может быть id проекта)
            List<ProcessAvatar> processes
                    = indexStructure.computeIfAbsent(index.getKey(), k -> new ArrayList<>());

            // если процессов нет, создаем список и вставляем в структуру

            // добавляем процесс
            processes.add(process);


        }
    }


    private void removeIndexes(ProcessAvatar processAvatar){

        var userIndexes = processAvatar.getIndexes();


        for (var index:userIndexes){

            // проверяем наличие корзины
            Map<String, List<ProcessAvatar>> namedBucket = indexes.get(index.getName());

            // по идее структура всегда присутствует, но на всякий случай проверяем
            if (namedBucket!=null){

                List<ProcessAvatar> processesAssociatedByKey = namedBucket.get(index.getKey());

                if (processesAssociatedByKey!=null){

                    // ссылка - одна и та же
                    processesAssociatedByKey.remove(processAvatar);

                    // очищаем список, если он пустой
                    if (processesAssociatedByKey.isEmpty()){
                        namedBucket.remove(index.getKey());
                    }
                }

                // очищаем пространство имен, если в нем больше нет никаких вторичных ключей
                if (namedBucket.isEmpty()){
                    indexes.remove(index.getName());
                }


            }


        }



    }



    // регистрируем процесс, при этом реализую прописанные пользователем индексы, если они есть
    public void registerAvatar(ProcessAvatar chainProcess){
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



    public Optional<ProcessAvatar> getAvatarById(UUID correlationId){
        readLock.lock();

        try {
            return Optional.ofNullable(allProcesses.get(correlationId));
        }
        finally {
            readLock.unlock();
        }
    }

    public ProcessAvatar getOrRestore(UUID correlationId,
                                      ProcessAvatar toRestore){






        writeLock.lock();

        try {
            if (allProcesses.containsKey(correlationId)) {
                ProcessAvatar chainProcess = allProcesses.get(correlationId);
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

    @Override
    public List<ProcessAvatar> getAll() {


        // read lock позволяет читать информацию множеству потоков,
        // но при этом он не позволяет write lock начать запись

        // write lock блокирует все - его будет ждать как readlock, так и другой write lock
        readLock.lock();

        try {
            return new ArrayList<>(allProcesses.values()); // copy
        }
        finally {
            readLock.unlock();
        }
    }

    @Override
    public Map<String, Map<String, List<ProcessAvatar>>> getIndexesStructure() {

        readLock.lock();



        try {

            // делаем deep copy

            HashMap<String, Map<String, List<ProcessAvatar>>> deep = new HashMap<>();

            for (var entry:indexes.entrySet()){

                String key = entry.getKey();

                Map<String, List<ProcessAvatar>> copiedValue = entry.getValue()
                        .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                                innerEntry->new ArrayList<>(innerEntry.getValue())
                                ));
                deep.put(key, copiedValue);
            }

            return deep;
        }

        finally {
            readLock.unlock();
        }

    }


    // очистка runtime окружения от terminated аватаров
    public void clearTerminatedAvatars(){



        writeLock.lock();

        try {


            Iterator<Map.Entry<UUID, ProcessAvatar>> iterator
                    = allProcesses.entrySet().iterator();


            while (iterator.hasNext()){

                var next = iterator.next();

                if (next.getValue().getStatus().get() == ProcessAvatarStatus.TERMINATED){


                    // удаляем индексы
                    removeIndexes(next.getValue());
                    // удаляем из основного хранилища
                    iterator.remove();


                }
            }



        }

        finally {
            writeLock.unlock();
        }


    }





}
