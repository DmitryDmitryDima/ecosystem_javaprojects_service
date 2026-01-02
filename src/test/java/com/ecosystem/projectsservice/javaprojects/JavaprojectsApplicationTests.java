package com.ecosystem.projectsservice.javaprojects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.processes.InternalEventsManager;
import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveInfo;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox.FileSaveOutboxEventChain;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox.FileSaveOutboxInitEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JavaprojectsApplicationTests {

	@Autowired
	private InternalEventsManager manager;

	@Autowired
	private FileSaveOutboxEventChain fileSaveOutboxEventChain;

	@Autowired
	private ObjectMapper mapper;

	@Test
	void contextLoads() {

        try {
            fileSaveOutboxEventChain.init(SecurityContext.builder().build(), RequestContext.builder().build(),
                    FileSaveInfo.builder()
                            .content("hello")
                            .projectsPath("///")
                            .fileId(1L)
                    .build()
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

}
