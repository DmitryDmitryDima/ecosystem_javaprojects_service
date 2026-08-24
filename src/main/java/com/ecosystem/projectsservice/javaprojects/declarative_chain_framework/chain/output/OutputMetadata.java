package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.output_actions.OutputAction;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step.ChainStep;



public class OutputMetadata<V> {


    // какая стадия работы цепи спровоцировала вызов процессора
    private OutputAction action;

    // параметры выполненного шага
    private ChainStep executedStep;

    // пространство для расширения
    private V value;






    public OutputAction getAction() {
        return this.action;
    }


    public ChainStep getExecutedStep() {
        return this.executedStep;
    }


    public V getValue() {
        return this.value;
    }


    public void setAction(final OutputAction action) {
        this.action = action;
    }


    public void setExecutedStep(final ChainStep executedStep) {
        this.executedStep = executedStep;
    }


    public void setValue(final V value) {
        this.value = value;
    }



    public OutputMetadata(final OutputAction action,
                          final ChainStep executedStep,
                          final V value) {
        this.action = action;
        this.executedStep = executedStep;
        this.value = value;
    }


    public OutputMetadata() {
    }







}
