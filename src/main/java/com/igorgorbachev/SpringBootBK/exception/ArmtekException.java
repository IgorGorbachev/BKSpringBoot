package com.igorgorbachev.SpringBootBK.exception;

public class ArmtekException extends RuntimeException {
    public ArmtekException(String message) {
        super(message);
    }

    public ArmtekException(String message, Throwable cause) {
        super(message, cause);
    }
}
