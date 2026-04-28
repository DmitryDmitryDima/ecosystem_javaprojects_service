package com.ecosystem.projectsservice.javaprojects.service.projects.state;


import com.ecosystem.projectsservice.javaprojects.dto.projects.state.Autosave;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ForcedSave;

// хуки для внесения изменений и уведомления о них внещних систем
public interface ContentStateUpdater {


    // при быстром сохранении - запись в поле кеша + уведомление
    void onAutosave(Autosave autosave);

    // при форсированном сохранении - обновление dto полностью
    void inForcedSave(ForcedSave save);


}
