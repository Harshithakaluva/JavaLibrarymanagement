package com.library.model;

import com.library.exceptions.BookNotAvailableException;

public class Book {
    // Encapsulation: nothing here is public
    private final String isbn;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;

    public Book(String isbn, String title, String author, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    // Behavior lives WITH the data it protects - core encapsulation idea.
    // Nobody outside this class can decrement availableCopies directly.
    public void decreaseAvailableCopies() throws BookNotAvailableException {
        if (availableCopies <= 0) {
            throw new BookNotAvailableException("No copies of \"" + title + "\" are currently available.");
        }
        availableCopies--;
    }

    public void increaseAvailableCopies() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    public void addCopies(int count) {
        totalCopies += count;
        availableCopies += count;
    }

    @Override
    public String toString() {
        return String.format("%-12s | %-30s | %-20s | %d/%d available",
                isbn, title, author, availableCopies, totalCopies);
    }
}
