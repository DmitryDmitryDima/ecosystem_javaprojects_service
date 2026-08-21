package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.exception.InvalidStructureException;

import java.util.Map;

public class BasicStructureValidator {

    private ChainStep<?> opening;

    private Map<String, ChainStep<?>> body;

    private ChainStep<?> ending;


    public BasicStructureValidator(ChainStep<?> opening,
                                   Map<String, ChainStep<?>> body, ChainStep<?> ending){

        this.body = body;
        this.ending = ending;
        this.opening = opening;

    }


    // todo алгоритм валидации, а также само устройство валидации рассмотрю после разработки цикличных
    public void validateStructure(){


        if (opening == null){
            throw new InvalidStructureException("missing chain opening pointer");
        }

        if (ending == null){
            throw new InvalidStructureException("missing chain ending pointer");
        }










    }





}
