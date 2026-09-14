package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure;


// данный класс позволяет вручную управлять ходом очереди

// императив имеет приоритет над декларативным
// другими словами
public class ChainRudder {

    // данное поле позволяет задать вручную следующий шаг
    private String next;


    // данное поле позволяет проигнорировать crash, перенаправив цепь на указанный шаг
    private String onCrash;


    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getOnCrash() {
        return onCrash;
    }

    public void setOnCrash(String onCrash) {
        this.onCrash = onCrash;
    }
}
