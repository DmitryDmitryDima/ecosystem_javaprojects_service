package com.ecosystem.projectsservice.javaprojects.external_messaging.message;



// external message should contain external context and external data

/*


напомню схему разделения - external message несет в себе external context, external data

chain ивент больше не привязан ни к контексту, ни к data, что означает,
что мы можем вставлять в него любые данные.
В специализированной цепочке мы можем навязать использование ивента, наследующего класс,
 в котором применяется как контекст, так и data

 */


import com.ecosystem.projectsservice.javaprojects.external_messaging.context.ExternalContext;
import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ExternalMessage {


    // текстовое сообщение
    private String message;

    private MessageStatus status;

    // тип (используется получателем)
    private String type;



    // контекст может использоваться сервисами посредниками
    private ExternalContext context;

    private ExternalData data;






}
