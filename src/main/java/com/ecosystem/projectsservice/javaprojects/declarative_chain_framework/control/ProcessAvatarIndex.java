package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control;





public class ProcessAvatarIndex {


    // имя индекса
    private String name;

    // ключ поиска
    private String key;


    // пример - name = projects - имя индекса, key - id проекта в строковой форме.
    // К вторичному ключу прилагается список непосредственных аватаров




    public String getName() {
        return this.name;
    }


    public String getKey() {
        return this.key;
    }


    public void setName(final String name) {
        this.name = name;
    }


    public void setKey(final String key) {
        this.key = key;
    }


    public ProcessAvatarIndex(final String name, final String key) {
        this.name = name;
        this.key = key;
    }


    public ProcessAvatarIndex() {
    }
}
