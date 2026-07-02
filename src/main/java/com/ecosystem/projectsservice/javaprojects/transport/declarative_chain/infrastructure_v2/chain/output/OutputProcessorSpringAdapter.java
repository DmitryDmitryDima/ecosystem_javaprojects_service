package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OutputProcessorSpringAdapter extends OutputProcessorDefault {


    @Override
    @Autowired
    public void setRepository(OutboxModelRepository repository) {
        super.setRepository(repository);
    }

    @Override
    @Autowired
    public void setMapper(MapperComponent mapper) {
        super.setMapper(mapper);
    }

    @PostConstruct
    public void post(){
        System.out.println("publisher here with repo ");
    }
}
