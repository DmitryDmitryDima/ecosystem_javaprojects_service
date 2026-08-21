package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.model.idempotency;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdempotencyModelJpaRepository extends JpaRepository<IdempotencyModelJpaEntity, UUID> {


}
