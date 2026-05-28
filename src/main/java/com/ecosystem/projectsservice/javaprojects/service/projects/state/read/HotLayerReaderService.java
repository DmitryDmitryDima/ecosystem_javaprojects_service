package com.ecosystem.projectsservice.javaprojects.service.projects.state.read;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedFile;
import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedJavaStructure;
import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedStructureJavaFile;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.suggestions.BasicSuggestion;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.suggestions.BasicSuggestionInfo;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.suggestions.SuggestedType;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileCache;
import com.ecosystem.projectsservice.javaprojects.service.cache.JavaStructureCacheService;
import com.ecosystem.projectsservice.javaprojects.service.external_values.StorageExternals;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.code.AccessModifier;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.code.CodeService;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageException;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.*;

/*
операции, связанные с чтением из "горячей" модели данных
 */
@Service
public class HotLayerReaderService implements HotLayerReader{


    // горячий слой
    @Autowired
    private FileCache fileCache;

    // холодный слой контента для подготовки горячего слоя
    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageExternals storageExternals;


    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private TransactionTemplate transaction;


    @Autowired
    private JavaStructureCacheService structureCache;

    @Autowired
    private CodeService codeService;







    @Override
    public FileDTO readFile(UUID projectId, UUID fileId) {


        Optional<CachedFile> cachedFileCheck = fileCache.get(fileId);

        CachedFile cachedFile;

        if (cachedFileCheck.isEmpty()){
            cachedFile = loadFileFromCold(projectId, fileId);
            // делаем прогрев
            fileCache.saveOrUpdate(cachedFile);
        }
        else {
            cachedFile = cachedFileCheck.get();
        }






        return FileDTO.builder()
                .id(cachedFile.getId())
                .name(cachedFile.getName())
                .extension(cachedFile.getExtension())
                .content(cachedFile.getContent())
                .constructedPath(cachedFile.getConstructedPath())
                .projectId(projectId)
                .build();
    }

    @Override
    public List<FileDTO> getAllHotFilesFromList(List<UUID> files) {

        List<FileDTO> answer = new ArrayList<>();

        for (var id:files){
            Optional<CachedFile> cachedFileCheck = fileCache.get(id);
            cachedFileCheck.ifPresent(cachedFile -> answer.add(FileDTO.builder()
                    .content(cachedFile.getContent())
                    .extension(cachedFile.getExtension())
                    .name(cachedFile.getName())
                    .constructedPath(cachedFile.getConstructedPath())
                    .id(cachedFile.getId())
                    .projectId(cachedFile.getProjectId())
                    .lastUpdate(cachedFile.getLastUpdate())
                    .build()));
        }



        return answer;
    }

    /*

    как уже было сделано ранее - предложка полагается на анализ внешней структуры файлов,
     а также на анализ внутренней структуры
     */

    // todo сейчас мы просто смотрим кешированную структуру, подбирая соответствующее имя
    // todo далее это будет опираться на положение
    // todo также мы долдны будем выгрузить из кеша сам файл,
    //  и в первую очередь проанализировать его контент

    @Override
    public BasicSuggestion basicSuggestion(BasicSuggestionInfo info) {


        Optional<CachedJavaStructure> structureCheck
                = structureCache.get(info.getProjectId());



        CachedJavaStructure cachedJavaStructure
                = structureCheck.orElseGet(()
                -> constructNewCachedStructure(info.getProjectId(), info.getRootId()));



        BasicSuggestion suggestion = new BasicSuggestion();


        // todo для демонстрации - только типы, соответствующие введенной пользователем строке
        for (Map.Entry<String, List<CachedStructureJavaFile>> pack:
                cachedJavaStructure.getStructure().entrySet()){


            String way = pack.getKey();

            for (var file:pack.getValue()){

                if (file.getName().startsWith(info.getUserText())){
                    suggestion.getTypes().add(SuggestedType
                            .builder()
                                    .path(way).name(file.getName()).build()
                            );
                }
            }
        }

        return suggestion;

















    }


    // строим структуру проекта на основе базы данных и файлового контента
    // todo анализ модификатора доступа должен быть максимально быстрым
    //  - чтобы это происходило в один поток
    private CachedJavaStructure constructNewCachedStructure(UUID projectId, UUID rootId){

        CachedJavaStructure cachedJavaStructure = new CachedJavaStructure();
        Map<String, List<CachedStructureJavaFile>> structure = new HashMap<>();

        cachedJavaStructure.setId(projectId);
        cachedJavaStructure.setStructure(structure);


        // извлекаем все java файлы проекта
        List<FileReadOnly> javaFiles = transaction.execute(status
                -> snapshotService.getAllFilesBelowDirectory(rootId)).stream()
                .filter(fileReadOnly -> "java".equals(fileReadOnly.getExtension()))
                .toList();



        // строим структуру
        for (var file:javaFiles){

            CachedStructureJavaFile cachedStructureFile = new CachedStructureJavaFile();
            cachedStructureFile.setId(file.getId());
            cachedStructureFile.setName(file.getName());

            // todo читаем контент, извлекая модификатор доступа
            cachedStructureFile.setModifier(AccessModifier.PUBLIC);


            String packagePath = codeService
                    .transformFileConstructedPathToPackage(file.getConstructed_path());


            structure.compute(packagePath, (k,v)->{
                if (v == null){
                    v = new ArrayList<>();
                }

                v.add(cachedStructureFile);

                return v;
            });






        }

        return cachedJavaStructure;



    }






    // загрузка из холодного слоя не всегда означает прогрев кеша
    private CachedFile loadFileFromCold(UUID projectId, UUID fileId){

        System.out.println("cold read");


        // проверяем базу данных

        Optional<FileReadOnly> dbCheck = snapshotService.getProjectFile(projectId, fileId);

        if (dbCheck.isEmpty()){
            throw new StateReadException("Файла не существует", "MISSING_FILE", HttpStatus.NOT_FOUND);
        }

        FileReadOnly metadata = dbCheck.get();

        if (metadata.isHidden() || metadata.getStatus() == FileStatus.REMOVING){
            throw new StateReadException("Файл недоступен для чтения", "FORBIDDEN_FILE", HttpStatus.FORBIDDEN);
        }

        String content;

        try {
            content = storageService.downloadContent(storageExternals.getStorageUserBucket(), fileId.toString());
        }

        catch (StorageException e){
            throw new StateReadException("Контент файла недоступен, попробуйте позже",
                    "MISSING_CONTENT",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }





        return CachedFile.builder()
                .id(fileId)
                .name(metadata.getName())
                .lastUpdate(Instant.now())
                .constructedPath(metadata.getConstructed_path())
                .version(0)
                .content(content)
                .extension(metadata.getExtension())
                .projectId(projectId)
                .build();
    }






}
