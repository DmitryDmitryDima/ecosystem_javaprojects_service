package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.enums.StepTimeUnit;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.ChainTimeUnit;

public class ChainUtils {



    public static Long convertToMillis(Long value, ChainTimeUnit unit){
        if (value == null) return null;
        return switch (unit){
            case MS->value;
            case SEC -> value*1000;
            case MIN -> value*1000*60;
            case HOURS -> value*1000*60*60;
            case DAYS -> value*1000*60*60*24;
        };
    }
}
