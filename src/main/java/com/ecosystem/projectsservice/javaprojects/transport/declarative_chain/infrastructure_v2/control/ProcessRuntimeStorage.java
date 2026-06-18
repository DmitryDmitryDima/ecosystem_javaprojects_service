package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control;

import java.util.Optional;
import java.util.UUID;

public interface ProcessRuntimeStorage {


    void registerChainProcess(DeclarativeChainProcess chainProcess);

    Optional<DeclarativeChainProcess> getChainProcessById(UUID correlationId);

    DeclarativeChainProcess getOrRestore(UUID correlationId,
                                         DeclarativeChainProcess toRestore);


}
