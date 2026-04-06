package com.ecosystem.projectsservice.javaprojects.repository.cache;

import com.ecosystem.projectsservice.javaprojects.model.cache.ProjectValidationHash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectValidationHashRepository
        extends CrudRepository<ProjectValidationHash, String>

{



}
