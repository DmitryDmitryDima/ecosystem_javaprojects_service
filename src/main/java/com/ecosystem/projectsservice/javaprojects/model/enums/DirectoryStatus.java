package com.ecosystem.projectsservice.javaprojects.model.enums;

// GENERATING - ЭТО ЗНАЧИТ, ЧТО ДОБАВЛЯЕТСЯ НОВЫЙ КОНТЕНТ

public enum DirectoryStatus {

    AVAILABLE, GENERATING, REMOVING, MIGRATING,

    // суть идеи - каждый шаг должен в начале поставить preparing флаг, и только после проверок проставить либо GENERATING, REMOVING, MIGRATING

    PREPARING_FOR_GENERATING, PREPARING_FOR_REMOVAL, PREPARING_FOR_MIGRATING
}
