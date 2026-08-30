package com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains;


import com.ecosystem.projectsservice.javaprojects.external_messaging.broadcast.MessageBroadcast;
import com.ecosystem.projectsservice.javaprojects.external_messaging.broadcast.MessageBroadcastException;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.ExternalContext;
import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.ExternalMessage;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastEnvelope {



    private MessageBroadcast broadcastInstance;

    private String messageType;

    private Class<? extends ExternalMessage> externalMessageClazz;





    public BroadcastResult sendSync(ExternallyConnectedChainEvent<?, ?> event,
                                    MessageStatus status){

        return sendSync(event.getExternalContext(),
                event.getExternalData(),
                event.getMessage(),
                status);

    }

    public BroadcastResult sendAsync(ExternallyConnectedChainEvent<?, ?> event,
                                     MessageStatus status){
        return sendAsync(event.getExternalContext(),
                event.getExternalData(),
                event.getMessage(),
                status);
    }



    public BroadcastResult sendSync(ExternalContext context,
                         ExternalData data,
                         String message,
                         MessageStatus status){




        try {
            broadcastInstance.sendSync(buildMessage(context, data, message, status));
            return new BroadcastResult(true, null);


        } catch (Exception e){

            return new BroadcastResult(false, e);
        }
    }

    public BroadcastResult sendAsync(ExternalContext context,
                          ExternalData data,
                          String message,
                          MessageStatus status
                          ){

        try {
            broadcastInstance.sendAsync(buildMessage(context, data, message, status));

            return new BroadcastResult(true, null);

        } catch (Exception e){

            return new BroadcastResult(false, e);
        }
    }




    private ExternalMessage buildMessage(ExternalContext context,
                                         ExternalData data,
                                         String textMessage,
                                         MessageStatus status) throws Exception
            {

        ExternalMessage message = externalMessageClazz.getConstructor().newInstance();

        message.setContext(context);
        message.setData(data);
        message.setMessage(textMessage);
        message.setStatus(status);
        message.setType(messageType);

        return message;



    }






}
