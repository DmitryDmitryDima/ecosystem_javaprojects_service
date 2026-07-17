package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control;

import java.util.Optional;
import java.util.UUID;

public interface ProcessAvatarStorage {


    void registerAvatar(ProcessAvatar chainProcess);

    Optional<ProcessAvatar> getAvatarById(UUID correlationId);

    ProcessAvatar getOrRestore(UUID correlationId,
                               ProcessAvatar toRestore);


}
