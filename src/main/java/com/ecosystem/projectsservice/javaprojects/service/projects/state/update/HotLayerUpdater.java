package com.ecosystem.projectsservice.javaprojects.service.projects.state.update;


import com.ecosystem.projectsservice.javaprojects.dto.projects.state.Autosave;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.CachedFileInvalidation;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ForcedSave;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ProjectStructureInvalidation;

// хуки, вносящие изменения в "горячий слой"
public interface HotLayerUpdater {


    // при быстром сохранении - запись в поле кеша + уведомление
    void onAutosave(Autosave autosave);

    // при форсированном сохранении - обновление dto полностью
    void onForcedSave(ForcedSave save);


    // инвалидация файловой записи
    void onFileInvalidate(CachedFileInvalidation fileInvalidation);


    void onProjectStructureInvalidate(ProjectStructureInvalidation structureInvalidation);


}
