package ru.MT.logic;

public class HTTPForbiddenException extends Exception{
    public HTTPForbiddenException(String message) {
        super(message);
    }

    public HTTPForbiddenException() {
        super();
    }
}
