package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.output_actions.OutputAction;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.ChainStep;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatar;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutputMetadata<V> {


    // какая стадия работы цепи спровоцировала вызов процессора

    private OutputAction action;

    private ChainStep<?> executedStep;




    private V value;




}
