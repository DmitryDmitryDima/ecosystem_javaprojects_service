package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.sender;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;

// место, куда отправляются расшифрованные и обработанные в менеджере ивенты
public interface ChainManagerSender {




    void send(ChainEvent event);





}
