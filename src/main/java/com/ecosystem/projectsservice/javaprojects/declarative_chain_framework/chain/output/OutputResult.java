package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output;

public class OutputResult {

    private boolean published;

    private Exception exception;

    private String message;

    public OutputResult(boolean published, Exception exception, String message) {
        this.published = published;
        this.exception = exception;
        this.message = message;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public static OutputResult success(){
        return new OutputResult(true, null, "Успешная публикация события");
    }
}
