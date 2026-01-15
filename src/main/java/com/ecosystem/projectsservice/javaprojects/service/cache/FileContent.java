package com.ecosystem.projectsservice.javaprojects.service.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FileContent {

    private Long id;

    private String text;

    private Instant lastUpdate;

    private boolean locked;
}
