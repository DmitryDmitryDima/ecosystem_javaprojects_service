package com.ecosystem.projectsservice.javaprojects.service.projects.state;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.Autosave;
import com.ecosystem.projectsservice.javaprojects.service.cache.external.FileCache;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// данный класс предоставляет хуки, вызываемые при совершении каких либо действий с кодовой базой
// хуки должны получить всю необходимую для изменения состояния/состояний информацию, поэтому выделяем отдельные dto
@Service
public class StateChangeListener {


    @Autowired
    private Broadcast broadcast;

    // сервис для db валидации
    @Autowired
    private SnapshotService snapshotService;

    // файловый кеш
    @Autowired
    private FileCache fileCache;









    // данное событие провоцирует точечное изменение в кешах, broadcast рассылку
    // если записи в кеше нет, то проверяется статус файла в бд,
    // после чего происходит создание новой записи

    public void onAutosave(Autosave autosave){

        /* шаг 1 - проверяем, есть ли запись в кеше. Держим в голове, что любая операция/цепочка,
         меняющая состояние файла/файлов - обязана инвалидировать затронутую сущность,
         тем самым провоцируя db валидацию через snapshot иерархии

         */



    }




}
