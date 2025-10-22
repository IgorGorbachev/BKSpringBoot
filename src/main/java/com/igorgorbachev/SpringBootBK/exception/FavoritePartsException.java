package com.igorgorbachev.SpringBootBK.exception;

public class FavoritePartsException extends RuntimeException{
    public FavoritePartsException(String message) {
        super(message);
    }

    public FavoritePartsException(String message, Throwable cause) {
        super(message, cause);
    }
}
