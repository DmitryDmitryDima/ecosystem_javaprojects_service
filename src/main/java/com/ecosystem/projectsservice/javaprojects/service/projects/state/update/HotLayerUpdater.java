package com.ecosystem.projectsservice.javaprojects.service.projects.state.update;


import com.ecosystem.projectsservice.javaprojects.dto.projects.state.*;

// хуки, вносящие изменения в "горячий слой"
public interface HotLayerUpdater {


    // при быстром сохранении - запись в поле кеша + уведомление
    void onAutosave(Autosave autosave);

    // при форсированном сохранении - обновление dto полностью
    void onForcedSave(ForcedSave save);

    void onFileMove(FileMove fileMove);


    // инвалидация файловой записи
    void fileInvalidation(CachedFileInvalidation fileInvalidation);


    void projectStructureInvalidation(ProjectStructureInvalidation structureInvalidation);


}
