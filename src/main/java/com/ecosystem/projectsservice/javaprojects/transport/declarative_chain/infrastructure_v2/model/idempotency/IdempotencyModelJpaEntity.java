package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.idempotency;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.time.Instant;
import java.util.UUID;


@Entity
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "idempotency_registry")
public class IdempotencyModelJpaEntity implements IdempotencyModel{


    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Strategy.class)
    private UUID id;



    // уникален в таблице
    @Column(unique = true)
    private UUID processId;









    @Override
    public UUID getProcessUUID() {
        return processId;
    }


}
