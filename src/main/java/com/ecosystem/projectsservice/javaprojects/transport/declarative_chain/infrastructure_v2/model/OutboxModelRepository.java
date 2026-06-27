package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


// контракт, позволяющий связать core функционал с конкретным способом доставки
public interface OutboxModelRepository {


    void save(OutboxModel model);





    // processed status callback ?
}
