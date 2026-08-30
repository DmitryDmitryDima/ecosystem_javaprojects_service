package com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.OutputResult;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.CompensationResult;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step.ChainStep;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step.StepExtension;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.chain.structure.DeclarativeChainSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.external_messaging.broadcast.MessageBroadcast;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.ExternalContext;
import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.ExternalMessage;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.declarative_messaging.MessageAfter;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.declarative_messaging.MessageBefore;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.declarative_messaging.MessageStepExtension;
import com.ecosystem.projectsservice.javaprojects.external_messaging.types.ExternalMessageType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

// должна быть задана категория внешнего сообщения и строковый тип ивентов (аннотацией)
public abstract class BroadcastableChain <E extends ExternallyConnectedChainEvent <? extends ExternalContext,
        ? extends ExternalData>>
        extends DeclarativeChainSpringAdapter <E> {





    private BroadcastEnvelope broadcastEnvelope = new BroadcastEnvelope();


    @Autowired
    private MessageBroadcast broadcast;









    public BroadcastEnvelope broadcast() {
        return broadcastEnvelope;
    }





    protected abstract Class<? extends ExternalMessage> messageBind();


    /*
    наполнение envelope
     */

    @Override
    protected void readChainStructure() {




        broadcastEnvelope.setBroadcastInstance(broadcast);
        broadcastEnvelope.setExternalMessageClazz(messageBind());








        ExternalMessageType messageTypeAnno
                = this.getClass().getAnnotation(ExternalMessageType.class);

        if (messageTypeAnno == null)
            throw new IllegalStateException("не указан тип внешнего сообщения");




        broadcastEnvelope.setMessageType(messageTypeAnno.type().getName());





        super.readChainStructure();
    }


    // помним, что в данном хуке уже все первоначальные настройки занесены


    // для ending шага автоматически вставляется message after со статусом success

    @Override
    protected void onStepRead(ChainStep aReadStep) {

        super.onStepRead(aReadStep);


        List<StepExtension> extensions = aReadStep.getExtensions();

        MessageStepExtension stepExtension = new MessageStepExtension();





        MessageAfter messageAfter = aReadStep.getMethod().getAnnotation(MessageAfter.class);

        if (messageAfter!=null){
            stepExtension.setMessageAfter(true);
        }

        MessageBefore messageBefore = aReadStep.getMethod().getAnnotation(MessageBefore.class);

        if (messageBefore!=null){
            stepExtension.setMessageBefore(true);
        }



        extensions.add(stepExtension);


    }

    @Override
    protected void chainFinalHook(E event, ChainStep step, ProcessAvatar avatar, OutputResult result) {
        super.chainFinalHook(event, step, avatar, result);

        if (result.isPublished()){

            System.out.println("Публикация соообщения об успешном завершении процесса!");
        }
    }

    @Override
    protected void afterCompensationHook(E event, ProcessAvatar avatar, CompensationResult result) {
        super.afterCompensationHook(event, avatar, result);




        System.out.println("Сообщение об ошибке. Последнее сообщение - "+event.getMessage()+"." +
                " Статус компенсации: "+result.getException());
    }

    @Override
    protected void beforeStepHook(E event, ChainStep step, ProcessAvatar avatar) {
        super.beforeStepHook(event, step, avatar);

        List<StepExtension> extensions = step.getExtensions();

        for (var extension:extensions){

            if (extension instanceof MessageStepExtension messageStepExtension){
                if (messageStepExtension.isMessageBefore()){
                    System.out.println("Публикация сообщения о том, что шаг "
                            +step.getName()+" сейчас будет выполнен");
                }
            }
        }
    }


    @Override
    protected void afterStepHook(E event, ChainStep step, ProcessAvatar avatar, OutputResult result) {
        super.afterStepHook(event, step, avatar, result);

        List<StepExtension> extensions = step.getExtensions();

        for (var extension:extensions){

            if (extension instanceof MessageStepExtension messageStepExtension){
                if (messageStepExtension.isMessageAfter() && result.isPublished()){
                    System.out.println("Публикация сообщения о том, что шаг "
                            +step.getName()+" был выполнен");
                }
            }
        }
    }





    // todo для компенсации (в конце) реализуется автоматическое сообщение в хуке со статусом error
}
