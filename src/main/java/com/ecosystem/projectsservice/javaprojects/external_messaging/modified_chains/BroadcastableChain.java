package com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.OutputResult;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.CompensationResult;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step.ChainStep;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step.StepExtension;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.chain.structure.DeclarativeChainSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.external_messaging.broadcast.MessageBroadcast;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.ExternalContext;
import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.ExternalMessage;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.MessageStatus;
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
    protected void chainFinalHook(E event,
                                  ChainStep step,
                                  ProcessAvatar avatar,
                                  OutputResult result) {
        super.chainFinalHook(event, step, avatar, result);

        if (result.isPublished()){

            broadcastEnvelope.sendSync(
                            event.getExternalContext(),
                    event.getExternalData(),
                    event.getMessage(),
                    MessageStatus.SUCCESS);

        }
    }

    @Override
    protected void afterCompensationHook(E event, ProcessAvatar avatar, CompensationResult result) {
        super.afterCompensationHook(event, avatar, result);


        Exception exception = result.getException();

        String compensationResultMessage = exception
                ==null?"Статус компенсации:успешно":"статус компенсации:ошибка - "+exception;




        broadcastEnvelope.sendSync(
                event.getExternalContext(),
                event.getExternalData(),
                event.getMessage()+" "+compensationResultMessage,
                MessageStatus.ERROR);





    }

    @Override
    protected void beforeStepHook(E event, ChainStep step, ProcessAvatar avatar) {
        super.beforeStepHook(event, step, avatar);

        List<StepExtension> extensions = step.getExtensions();

        for (var extension:extensions){

            if (extension instanceof MessageStepExtension messageStepExtension){
                if (messageStepExtension.isMessageBefore()){
                    broadcastEnvelope.sendSync(
                            event.getExternalContext(),
                            event.getExternalData(),
                            event.getMessage(),
                            MessageStatus.PROCESSING);
                }
            }
        }
    }


    // помним, что этот хук выполняет только после успешного выполнения шага

    @Override
    protected void afterStepHook(E event,
                                 ChainStep step,
                                 ProcessAvatar avatar,
                                 OutputResult result) {
        super.afterStepHook(event, step, avatar, result);

        List<StepExtension> extensions = step.getExtensions();

        for (var extension:extensions){

            if (extension instanceof MessageStepExtension messageStepExtension){
                if (messageStepExtension.isMessageAfter() && result.isPublished()){
                    broadcastEnvelope.sendSync(
                            event.getExternalContext(),
                            event.getExternalData(),
                            event.getMessage(),
                            MessageStatus.PROCESSING);
                }
            }
        }
    }






}
