package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;


import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SimpleProjectTrigger extends Trigger {

    private Long projectId;

    // if null - all members inside project should receive trigger message
    private Long fileId;

    // Мы должны получить какой-либо ответ (да или нет) от всех участников опроса
    // на основе стратегии мы принимаем решение (при получении каждого из ответов мы опираемся на соотношение true/false)
    private HashMap<UUID, Boolean> opinions;










}
