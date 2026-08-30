package com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastResult {

    private boolean success;

    private Exception exception;


}
