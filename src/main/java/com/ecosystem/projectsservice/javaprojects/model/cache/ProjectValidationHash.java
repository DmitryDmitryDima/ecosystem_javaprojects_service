package com.ecosystem.projectsservice.javaprojects.model.cache;




import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.UUID;



// кешируем валидационные данные
// данный объект живет в кеше одну минуту

@RedisHash(value = "project_validation",
        timeToLive = 60)
@Getter
@Setter
@NoArgsConstructor

public class ProjectValidationHash {




    @Id
    private String id;

    private UUID projectOwner;

    //@Indexed - аннотация, позволяющая искать записи по полю в хеше


}
