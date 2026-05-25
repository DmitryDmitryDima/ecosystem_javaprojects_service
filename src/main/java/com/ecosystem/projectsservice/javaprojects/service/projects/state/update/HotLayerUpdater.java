package com.ecosystem.projectsservice.javaprojects.service.projects.state.update;


import com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates.*;

// хуки, вносящие изменения в "горячий слой"
public interface HotLayerUpdater {


    // при быстром сохранении - запись в поле кеша + уведомление
    void onAutosave(Autosave autosave);

    // при форсированном сохранении - обновление dto полностью
    void onForcedSave(ForcedSave save);

    void onFileMove(FileMove fileMove);

    void onDirectoryRemoval(DirectoryRemoval directoryRemoval);

    void onDirectoryMove(DirectoryMove directoryMove);


    // инвалидация файловой записи
    void fileInvalidation(CachedFileInvalidation fileInvalidation);

    void filesInvalidation(CachedFilesInvalidation filesInvalidation);


    void projectStructureInvalidation(ProjectStructureInvalidation structureInvalidation);


}
