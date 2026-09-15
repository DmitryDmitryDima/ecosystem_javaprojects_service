package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure;


// данный класс позволяет вручную управлять ходом очереди

// императив имеет приоритет над декларативным
// другими словами
public class ChainRudder {

    // данное поле позволяет задать вручную следующий шаг
    private String next;


    // данное поле позволяет проигнорировать ошибку в шаге,
    // перенаправив цепь на вручную указанный шаг
    private String onStepCrash;


    // если возникла ошибка invalid path, то можно вручную указать другой шаг
    // пример - ai выдал недействительный шаг
    private String onInvalidPath;

    // позволяет указать шаг, идущий после loop overflow - для того, чтобы обойти дефолтную компенсацию
    private String onLoopOverflow;





    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getOnStepCrash() {
        return onStepCrash;
    }

    public void setOnStepCrash(String onCrash) {
        this.onStepCrash = onCrash;
    }


    public String getOnInvalidPath() {
        return onInvalidPath;
    }

    public void setOnInvalidPath(String onInvalidPath) {
        this.onInvalidPath = onInvalidPath;
    }

    public String getOnLoopOverflow() {
        return onLoopOverflow;
    }

    public void setOnLoopOverflow(String onLoopOverflow) {
        this.onLoopOverflow = onLoopOverflow;
    }
}
