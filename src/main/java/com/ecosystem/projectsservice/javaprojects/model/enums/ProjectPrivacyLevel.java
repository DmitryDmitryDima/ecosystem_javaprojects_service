package com.ecosystem.projectsservice.javaprojects.model.enums;

// PRIVATE - ВИДЕН ТОЛЬКО ХОЗЯИНУ И УЧАСТНИКАМ
// OPEN - ВИДЕН ВСЕМ, НО ЭТО НЕ ОЗНАЧАЕТ ПРАВА НА ЧТЕНИЕ - КАЖДЫЙ МОЖЕТ УВИДЕТЬ ЛИШЬ ВНЕШНЕЕ ОПИСАНИЕ И ПОДАТЬ ЗАЯВКУ НА ВСТУПЛЕНИЕ
// OPEN статус также позволяет скопировать проект себе для запуска
public enum ProjectPrivacyLevel {

    PRIVATE, OPEN
}
