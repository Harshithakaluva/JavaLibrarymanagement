package com.library.exceptions;

public class MaxBooksLimitException extends Exception {
    public MaxBooksLimitException(String message) {
        super(message);
    }
}
