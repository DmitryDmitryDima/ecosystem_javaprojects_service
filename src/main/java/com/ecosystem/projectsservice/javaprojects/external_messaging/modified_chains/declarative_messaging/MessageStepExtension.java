package com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.declarative_messaging;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step.StepExtension;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.MessageStatus;

public class MessageStepExtension implements StepExtension {

    private boolean messageBefore;

    private boolean messageAfter;


    // processing - обычный шаг

    // error - после компенсации




    public MessageStepExtension(){}

    public MessageStepExtension(boolean messageBefore, boolean messageAfter) {
        this.messageBefore = messageBefore;
        this.messageAfter = messageAfter;

    }

    public boolean isMessageBefore() {
        return messageBefore;
    }

    public void setMessageBefore(boolean messageBefore) {
        this.messageBefore = messageBefore;
    }

    public boolean isMessageAfter() {
        return messageAfter;
    }

    public void setMessageAfter(boolean messageAfter) {
        this.messageAfter = messageAfter;
    }


}
