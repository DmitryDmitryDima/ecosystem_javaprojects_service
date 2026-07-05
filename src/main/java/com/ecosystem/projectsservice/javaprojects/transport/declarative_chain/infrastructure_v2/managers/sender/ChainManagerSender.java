package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;

// место, куда отправляются расшифрованные и обработанные в менеджере ивенты
public interface ChainManagerSender {




    void send(ChainEvent event);





}
