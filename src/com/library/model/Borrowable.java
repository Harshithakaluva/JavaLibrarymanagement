package com.library.model;

import com.library.exceptions.MaxBooksLimitException;

/**
 * Abstraction: any class that can borrow/return books must implement this.
 * We depend on this interface elsewhere, not on the concrete Member class.
 */
public interface Borrowable {
    void borrowBook(Book book) throws MaxBooksLimitException;
    void returnBook(Book book);
    int getBorrowedCount();
}
