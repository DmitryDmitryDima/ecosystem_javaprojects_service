package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.model.idempotency;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.idempotency.IdempotencyModel;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.util.UUID;


@Entity
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


    @Override
    public String toString(){

        return "process id idempotency is "+processId;
    }



    public static IdempotencyModelJpaEntityBuilder builder() {
        return new IdempotencyModelJpaEntityBuilder();
    }


    public void setId(final UUID id) {
        this.id = id;
    }


    public void setProcessId(final UUID processId) {
        this.processId = processId;
    }


    public IdempotencyModelJpaEntity() {
    }


    public IdempotencyModelJpaEntity(final UUID id, final UUID processId) {
        this.id = id;
        this.processId = processId;
    }


    public static class IdempotencyModelJpaEntityBuilder {

        private UUID id;

        private UUID processId;


        IdempotencyModelJpaEntityBuilder() {
        }


        public IdempotencyModelJpaEntityBuilder id(final UUID id) {
            this.id = id;
            return this;
        }


        public IdempotencyModelJpaEntityBuilder processId(final UUID processId) {
            this.processId = processId;
            return this;
        }


        public IdempotencyModelJpaEntity build() {
            return new IdempotencyModelJpaEntity(this.id, this.processId);
        }



    }





}
