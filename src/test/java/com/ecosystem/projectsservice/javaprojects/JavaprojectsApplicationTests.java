package com.ecosystem.projectsservice.javaprojects;

import com.ecosystem.projectsservice.javaprojects.processes.InternalEventsManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JavaprojectsApplicationTests {

	@Autowired
	private InternalEventsManager manager;

	@Test
	void contextLoads() {
		manager.getEventByName("");
	}

}
