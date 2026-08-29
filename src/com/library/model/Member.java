package com.library.model;

import com.library.exceptions.MaxBooksLimitException;

import java.util.ArrayList;
import java.util.List;

public class Member extends Person implements Borrowable {

    private static final int MAX_BOOKS_ALLOWED = 3;

    private final List<Book> borrowedBooks;

    public Member(String id, String name, String email) {
        super(id, name, email); // reuse Person's constructor - inheritance
        this.borrowedBooks = new ArrayList<>();
    }

    // Polymorphism: overriding the abstract method from Person
    @Override
    public String getRole() {
        return "Member";
    }

    @Override
    public void borrowBook(Book book) throws MaxBooksLimitException {
        if (borrowedBooks.size() >= MAX_BOOKS_ALLOWED) {
            throw new MaxBooksLimitException(
                    getName() + " has already borrowed the maximum of " + MAX_BOOKS_ALLOWED + " books.");
        }
        borrowedBooks.add(book);
    }

    @Override
    public void returnBook(Book book) {
        borrowedBooks.remove(book);
    }

    @Override
    public int getBorrowedCount() {
        return borrowedBooks.size();
    }

    public List<Book> getBorrowedBooks() {
        return new ArrayList<>(borrowedBooks); // return a copy - protects internal state
    }
}
