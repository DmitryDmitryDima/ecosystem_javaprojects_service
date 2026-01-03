package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveInfo;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox.FileSaveOutboxEventChain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ChainsTest {

    @Autowired
    private FileSaveOutboxEventChain fileSaveOutboxEventChain;

    @Test
    public void testChain(){
        try {
            fileSaveOutboxEventChain.init(SecurityContext.builder().build(), RequestContext.builder().build(),
                    FileSaveInfo.builder()
                            .content("hello")
                            .projectsPath("C:/Users/")
                            .fileId(20L)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
