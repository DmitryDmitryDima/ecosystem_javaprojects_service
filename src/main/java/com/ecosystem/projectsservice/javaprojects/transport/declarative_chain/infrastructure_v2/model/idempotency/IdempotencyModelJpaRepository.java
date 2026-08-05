package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.idempotency;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdempotencyModelJpaRepository extends JpaRepository<IdempotencyModelJpaEntity, UUID> {


}
