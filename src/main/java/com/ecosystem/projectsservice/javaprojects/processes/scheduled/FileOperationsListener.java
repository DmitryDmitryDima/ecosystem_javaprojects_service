package com.ecosystem.projectsservice.javaprojects.processes.scheduled;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileContentCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

// фоновые процессы, ассоциированные с файлами в проектах
@Service
public class FileOperationsListener {


    @Autowired
    private FileContentCache<FileDTO, Long> fileContentCache;


    /*
    периодически записываем в диск
     */
    @Scheduled(fixedDelay = 30000)
    public void fileDiskWriteOperations(){

    }


}
