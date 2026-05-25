package com.ecosystem.projectsservice.javaprojects.service.projects.state.read;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.suggestions.BasicSuggestion;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.suggestions.BasicSuggestionRequest;

import java.util.List;
import java.util.UUID;

// чтение данных из горячего слоя (со способностью подгрузки холодного)

// анализ кода происходит через кешированную структуру, в которой мы, в свою очередь,
// обнаруживаем ссылки на файлы
public interface HotLayerReader {


    // чтение файла
    FileDTO readFile(UUID projectId, UUID fileId);


    // возвращаем файлы, присутствующие в горячем слое среди присланных
    List<FileDTO> getAllHotFilesFromList(List<UUID> files);

    // dot suggestion





    // basic suggestion

    BasicSuggestion basicSuggestion(BasicSuggestionRequest request);
}
