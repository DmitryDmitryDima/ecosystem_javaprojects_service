package com.ecosystem.projectsservice.javaprojects.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    /*
     - waiting - состояние события между созданием и чтением в обработчике
     - processing - событие было прочитано и опубликовано в application listener. Внутренний ивент должен в конце,
       в независимости от исхода, изменить этот статус на processed. Этот же ивент должен создать новое событие для outbox, двигая цепочку дальше
       к следующему шагу (следующий ивент или компенсация). В любом случае все должно кончится публикацией для rabbitmq
       для поимки зависших в этом статусе событий мы должны иметь некий timestamp, last touch, по которому мы будем понимать, что дальше ждать нет смысла
       Что делать в таком сценарии - отдельный вопрос.
     - processed - работа с событием закончена, оно больше не актуально, нужно только для истории и может быть стерто. Проставляется внутренним ивентом
     */


    public static enum OutboxEventStatus {PROCESSING, PROCESSED, WAITING}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OutboxEvent.OutboxEventStatus status;



    // тип ивента - его string форма хранится в каждом из ивентов. Тип + payload => application event для публикации в цепочку после прочтения из outbox
    private String type;





    // json репрезентация ивента
    @Column(columnDefinition = "TEXT")
    private String payload;

    // последняя смена статуса
    private Instant last_update;


}
