package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.status_groups.DeliveryStatus;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.status_groups.PerformanceStatus;
import lombok.*;


public class ChainEventProcessingInfo {



    // ПАРАМЕТРЫ ХОДА ПРОЦЕССА


    private long currentRetry = 0;

    // текущий шаг - зная его, мы можем перейти к следующему
    private String currentStep; // если null - это первый шаг


    // в каком качестве был прочитан ивент
    private DeliveryStatus deliveryStatus;

    // что произошло с процессом в момент создания outbox ивента
    private PerformanceStatus performanceStatus;


    public ChainEventProcessingInfo(final long currentRetry,
                                    final String currentStep,
                                    final DeliveryStatus deliveryStatus,
                                    final PerformanceStatus performanceStatus) {
        this.currentRetry = currentRetry;
        this.currentStep = currentStep;
        this.deliveryStatus = deliveryStatus;
        this.performanceStatus = performanceStatus;
    }


    public ChainEventProcessingInfo() {
    }





    public long getCurrentRetry() {
        return this.currentRetry;
    }


    public String getCurrentStep() {
        return this.currentStep;
    }


    public DeliveryStatus getDeliveryStatus() {
        return this.deliveryStatus;
    }


    public PerformanceStatus getPerformanceStatus() {
        return this.performanceStatus;
    }


    public void setCurrentRetry(final long currentRetry) {
        this.currentRetry = currentRetry;
    }


    public void setCurrentStep(final String currentStep) {
        this.currentStep = currentStep;
    }


    public void setDeliveryStatus(final DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }


    public void setPerformanceStatus(final PerformanceStatus performanceStatus) {
        this.performanceStatus = performanceStatus;
    }



    public static ChainEventProcessingInfoBuilder builder() {
        return new ChainEventProcessingInfoBuilder();
    }




    public static class ChainEventProcessingInfoBuilder {

        private long currentRetry;

        private String currentStep;

        private DeliveryStatus deliveryStatus;

        private PerformanceStatus performanceStatus;


        ChainEventProcessingInfoBuilder() {
        }


        public ChainEventProcessingInfoBuilder currentRetry(final long currentRetry) {
            this.currentRetry = currentRetry;
            return this;
        }


        public ChainEventProcessingInfoBuilder currentStep(final String currentStep) {
            this.currentStep = currentStep;
            return this;
        }


        public ChainEventProcessingInfoBuilder deliveryStatus(final DeliveryStatus deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }


        public ChainEventProcessingInfoBuilder performanceStatus(final PerformanceStatus performanceStatus) {
            this.performanceStatus = performanceStatus;
            return this;
        }


        public ChainEventProcessingInfo build() {
            return new ChainEventProcessingInfo(this.currentRetry, this.currentStep, this.deliveryStatus, this.performanceStatus);
        }


    }
}
