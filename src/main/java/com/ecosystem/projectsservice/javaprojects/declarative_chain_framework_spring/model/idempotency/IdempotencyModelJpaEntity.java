package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.model.idempotency;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.idempotency.IdempotencyModel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.util.UUID;


@Entity
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "idempotency_registry")
public class IdempotencyModelJpaEntity implements IdempotencyModel {


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
