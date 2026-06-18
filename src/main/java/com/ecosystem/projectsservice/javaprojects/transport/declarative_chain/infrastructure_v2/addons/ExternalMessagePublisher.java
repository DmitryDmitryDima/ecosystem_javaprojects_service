package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.addons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

@Service
public class ExternalMessagePublisher {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ObjectMapper mapper;




    public void publishExternalMessage(ExternalMessagePublisherInfo info){


    }
}
