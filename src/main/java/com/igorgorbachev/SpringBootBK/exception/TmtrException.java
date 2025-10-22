package com.igorgorbachev.SpringBootBK.exception;

public class TmtrException extends RuntimeException {
    public TmtrException(String message) {
        super(message);
    }

    public TmtrException(String message, Throwable cause) {
        super(message, cause);
    }
}
