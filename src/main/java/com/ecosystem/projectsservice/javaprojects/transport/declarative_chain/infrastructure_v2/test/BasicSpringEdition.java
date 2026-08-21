package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.OutputProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BasicSpringEdition extends Basic {


    public BasicSpringEdition(@Autowired OutputProcessor publisher) {
        super(publisher);





    }



}
