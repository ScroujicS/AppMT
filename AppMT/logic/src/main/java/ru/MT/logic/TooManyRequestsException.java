package ru.MT.logic;

public class TooManyRequestsException extends Exception{
    public TooManyRequestsException(String message) {
        super(message);
    }

    public TooManyRequestsException() {
        super();
    }
}
