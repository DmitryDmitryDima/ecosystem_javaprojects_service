package com.ecosystem.projectsservice.javaprojects.service.storage;



// данный сервис предназначен для взаимодействия с холодным облачным хранилищем
// должен поддерживать различные сценарии взаимодействия
public interface StorageService {

    // метод для сохранения строкового контента
    void saveOrUpdate(String bucket, String key, String content)
            throws StorageException;


    // удаляем контент из хранилища
    void delete(String bucket, String key);


    // читаем контент (в данном сервисе, по идее, не должно быть больших файлов)
    String downloadContent(String bucket, String key) throws StorageException;




}
