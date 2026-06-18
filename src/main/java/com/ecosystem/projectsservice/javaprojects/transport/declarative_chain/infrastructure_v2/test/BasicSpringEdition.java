package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher.ChainPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BasicSpringEdition extends Basic {


    public BasicSpringEdition(@Autowired  ChainPublisher publisher) {
        super(publisher);





    }


    public void check(){
        getPublisher().publish(null, null);
    }
}
