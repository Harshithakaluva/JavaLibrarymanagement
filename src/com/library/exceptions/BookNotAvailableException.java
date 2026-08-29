package com.library.exceptions;

public class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String message) {
        super(message);
    }
}
