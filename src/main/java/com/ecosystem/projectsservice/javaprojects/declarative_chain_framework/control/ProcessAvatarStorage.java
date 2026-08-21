package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ProcessAvatarStorage {


    void registerAvatar(ProcessAvatar chainProcess);

    Optional<ProcessAvatar> getAvatarById(UUID correlationId);

    ProcessAvatar getOrRestore(UUID correlationId,
                               ProcessAvatar toRestore);


    List<ProcessAvatar> getAll();


    Map<String, Map<String, List<ProcessAvatar>>> getIndexesStructure();


}
