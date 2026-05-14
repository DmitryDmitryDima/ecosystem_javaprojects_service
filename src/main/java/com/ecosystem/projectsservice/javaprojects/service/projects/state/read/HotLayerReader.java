package com.ecosystem.projectsservice.javaprojects.service.projects.state.read;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;

import java.util.UUID;

// чтение данных из горячего слоя (со способностью подгрузки холодного)

// анализ кода происходит через кешированную структуру, в которой мы, в свою очередь,
// обнаруживаем ссылки на файлы
public interface HotLayerReader {



    FileDTO readFile(UUID projectId, UUID fileId);

    // dot suggestion

    // basic suggestion
}
