package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.utils;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.ChainTimeUnit;

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
