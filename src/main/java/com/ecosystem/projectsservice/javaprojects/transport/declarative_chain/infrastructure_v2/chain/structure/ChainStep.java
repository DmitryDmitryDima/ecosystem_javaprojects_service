package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.ChainTimeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChainStep <Extension> {


    // main info

    private String name; // указывается для каждого шага пользователем

    private String next; // указывается для каждого шага, кроме ending

    private Method method; // сохраняется в runtime для reflection запуска шагов


    // control info

    private Long retry;

    private ChainTimeUnit timeLimitUnit;

    private Long timeLimit;

    private ChainTimeUnit waitingForSignalUnit;

    private Long waitingForSignal;

    private boolean everlasting;




    // additionals

    private Extension extensions;


}
