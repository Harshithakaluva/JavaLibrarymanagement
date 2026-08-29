package com.library.service;

import com.library.exceptions.*;
import com.library.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Library "has-a" collection of Books and Members (composition).
 * This is the single place that coordinates rules between them.
 */
public class Library {

    private final String name;
    private final Map<String, Book> catalog = new HashMap<>();     // isbn -> Book
    private final Map<String, Member> members = new HashMap<>();   // id -> Member
    private final List<Transaction> transactionLog = new ArrayList<>();

    public Library(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // ---------- Catalog management ----------

    public void addBook(Book book) {
        // If the ISBN already exists, just add more copies instead of duplicating
        if (catalog.containsKey(book.getIsbn())) {
            catalog.get(book.getIsbn()).addCopies(book.getTotalCopies());
        } else {
            catalog.put(book.getIsbn(), book);
        }
    }

    public void removeBook(String isbn) throws BookNotFoundException {
        if (!catalog.containsKey(isbn)) {
            throw new BookNotFoundException("No book found with ISBN " + isbn);
        }
        catalog.remove(isbn);
    }

    public Book findBookByIsbn(String isbn) throws BookNotFoundException {
        Book book = catalog.get(isbn);
        if (book == null) {
            throw new BookNotFoundException("No book found with ISBN " + isbn);
        }
        return book;
    }

    public List<Book> searchByTitle(String keyword) {
        List<Book> results = new ArrayList<>();
        for (Book b : catalog.values()) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(b);
            }
        }
        return results;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(catalog.values());
    }

    // ---------- Member management ----------

    public void registerMember(Member member) {
        members.put(member.getId(), member);
    }

    public Member findMemberById(String id) throws MemberNotFoundException {
        Member member = members.get(id);
        if (member == null) {
            throw new MemberNotFoundException("No member found with ID " + id);
        }
        return member;
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members.values());
    }

    // ---------- Core operations ----------

    public void issueBook(String memberId, String isbn)
            throws MemberNotFoundException, BookNotFoundException,
                   BookNotAvailableException, MaxBooksLimitException {

        Member member = findMemberById(memberId);
        Book book = findBookByIsbn(isbn);

        // Order matters: check the member's limit before touching the book's stock,
        // so a failed borrow never leaves the book's copy count decremented.
        member.borrowBook(book);          // may throw MaxBooksLimitException
        book.decreaseAvailableCopies();   // may throw BookNotAvailableException

        transactionLog.add(new Transaction(book, member, Transaction.Type.ISSUE));
    }

    public void returnBook(String memberId, String isbn)
            throws MemberNotFoundException, BookNotFoundException {

        Member member = findMemberById(memberId);
        Book book = findBookByIsbn(isbn);

        member.returnBook(book);
        book.increaseAvailableCopies();

        transactionLog.add(new Transaction(book, member, Transaction.Type.RETURN));
    }

    public List<Transaction> getTransactionLog() {
        return new ArrayList<>(transactionLog);
    }
}
