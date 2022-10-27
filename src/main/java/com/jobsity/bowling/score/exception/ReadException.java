package com.jobsity.bowling.score.exception;

public class ReadException extends Exception {

    public ReadException(String message) {
        super(message);
    }

    public ReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
