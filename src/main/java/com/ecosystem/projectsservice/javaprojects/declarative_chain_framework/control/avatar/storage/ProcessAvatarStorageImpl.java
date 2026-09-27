package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.storage;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.structure.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.structure.ProcessAvatarIndex;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.structure.ProcessAvatarStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

// бывший process aggregator
public class ProcessAvatarStorageImpl implements ProcessAvatarStorage {


    // все процессы
    private final Map<UUID, ProcessAvatar> allProcesses = new ConcurrentHashMap<>();


    // пользовательские индексы

    // мы используем имя индекса, как ключ, и вторичный ключ, как ключ к внутренней таблице
    // допускается, что на один ключ может быть несколько процессов
    private final Map<String,
            Map<String, Set<ProcessAvatar>>> indexes = new ConcurrentHashMap<>();






    private void createIndexes(ProcessAvatar process){


        for (var index:process.getIndexes()){

            // атомарная вставка с созданием недостающих структуру

            indexes
                    .computeIfAbsent(index.getName(),
                            k->new ConcurrentHashMap<>())
                    .computeIfAbsent(index.getKey(),
                            k->ConcurrentHashMap.newKeySet())
                    .add(process);
        }



    }


    private void removeIndexes(ProcessAvatar processAvatar){

        var userIndexes = processAvatar.getIndexes();


        for (var index:userIndexes){

            // проверяем наличие корзины
            Map<String, Set<ProcessAvatar>> namedBucket = indexes.get(index.getName());

            // по идее структура всегда присутствует, но на всякий случай проверяем
            if (namedBucket!=null){

                Set<ProcessAvatar> processesAssociatedByKey = namedBucket.get(index.getKey());

                if (processesAssociatedByKey!=null){

                    // ссылка - одна и та же
                    processesAssociatedByKey.remove(processAvatar);

                    // очищаем список, если он пустой
                    if (processesAssociatedByKey.isEmpty()){
                        // удаляем конкретную запись
                        namedBucket.remove(index.getKey(), processesAssociatedByKey);
                    }
                }

                // очищаем пространство имен, если в нем больше нет никаких вторичных ключей
                if (namedBucket.isEmpty()){
                    indexes.remove(index.getName(), namedBucket);
                }


            }


        }



    }



    // регистрируем процесс, при этом реализую прописанные пользователем индексы, если они есть
    public void registerAvatar(ProcessAvatar chainProcess){



        // вставляем значение только если его нет

        var old = allProcesses.putIfAbsent(chainProcess.getCorrelationId(), chainProcess);

        // если значение было, значит ничего не было вставлено, значит попытка провальна
        if (old!=null){
            throw new IllegalStateException("Process already registered");
        }




        createIndexes(chainProcess);








    }



    public Optional<ProcessAvatar> getAvatarById(UUID correlationId){


        return Optional.ofNullable(allProcesses.get(correlationId));


    }



    public ProcessAvatar getOrRestore(UUID correlationId,
                                      ProcessAvatar toRestore){




        var old = allProcesses.putIfAbsent(correlationId, toRestore);

        // если предыдущий равен null - значит, что произошел restore
        if (old == null){
            createIndexes(toRestore);
            return toRestore;
        }

        return old;













    }

    @Override
    public List<ProcessAvatar> getAll() {


        return new ArrayList<>(allProcesses.values());



    }

    @Override
    public Map<String, Map<String, List<ProcessAvatar>>> getIndexesStructure() {

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


    // очистка runtime окружения от terminated аватаров
    public void clearTerminatedAvatars(){






        allProcesses.entrySet().removeIf(entry->{


            var avatar = entry.getValue();

            if (avatar.getStatus().get() == ProcessAvatarStatus.TERMINATED){
                removeIndexes(avatar);
                return true;
            }

            return false;
        });














    }





}
