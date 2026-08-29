package com.library.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    public enum Type { ISSUE, RETURN }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Book book;       // Composition: a Transaction "has-a" Book
    private final Member member;   // and "has-a" Member
    private final Type type;
    private final LocalDateTime timestamp;

    public Transaction(Book book, Member member, Type type) {
        this.book = book;
        this.member = member;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }

    public Type getType() {
        return type;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s - \"%s\"",
                timestamp.format(FORMATTER), type, member.getName(), book.getTitle());
    }
}
