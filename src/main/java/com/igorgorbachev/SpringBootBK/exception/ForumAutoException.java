package com.igorgorbachev.SpringBootBK.exception;

public class ForumAutoException extends RuntimeException {
    public ForumAutoException(String message) {
        super(message);
    }

    public ForumAutoException(String message, Throwable cause) {
        super(message, cause);
    }
}
