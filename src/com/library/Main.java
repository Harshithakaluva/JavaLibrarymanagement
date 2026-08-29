package com.library;

import com.library.exceptions.*;
import com.library.model.*;
import com.library.service.Library;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Library library = new Library("City Central Library");
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        seedData();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listBooks();
                case "2" -> addBook();
                case "3" -> registerMember();
                case "4" -> issueBook();
                case "5" -> returnBook();
                case "6" -> listMembers();
                case "7" -> viewTransactionLog();
                case "0" -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
        scanner.close();
    }

    private static void seedData() {
        library.addBook(new Book("ISBN001", "Clean Code", "Robert C. Martin", 3));
        library.addBook(new Book("ISBN002", "Effective Java", "Joshua Bloch", 2));
        library.addBook(new Book("ISBN003", "The Pragmatic Programmer", "Andrew Hunt", 1));

        library.registerMember(new Member("M001", "Asha Rao", "asha@example.com"));
        library.registerMember(new Member("M002", "Vikram Singh", "vikram@example.com"));
    }

    private static void printMenu() {
        System.out.println("\n===== " + library.getName() + " =====");
        System.out.println("1. List all books");
        System.out.println("2. Add a new book");
        System.out.println("3. Register a new member");
        System.out.println("4. Issue a book");
        System.out.println("5. Return a book");
        System.out.println("6. List all members");
        System.out.println("7. View transaction log");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void listBooks() {
        List<Book> books = library.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books in the catalog.");
            return;
        }
        books.forEach(System.out::println);
    }

    private static void addBook() {
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Number of copies: ");
        int copies = readInt();

        library.addBook(new Book(isbn, title, author, copies));
        System.out.println("Book added successfully.");
    }

    private static void registerMember() {
        System.out.print("Member ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        library.registerMember(new Member(id, name, email));
        System.out.println("Member registered successfully.");
    }

    private static void issueBook() {
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        System.out.print("Book ISBN: ");
        String isbn = scanner.nextLine().trim();

        try {
            library.issueBook(memberId, isbn);
            System.out.println("Book issued successfully.");
        } catch (MemberNotFoundException | BookNotFoundException
                 | BookNotAvailableException | MaxBooksLimitException e) {
            System.out.println("Could not issue book: " + e.getMessage());
        }
    }

    private static void returnBook() {
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        System.out.print("Book ISBN: ");
        String isbn = scanner.nextLine().trim();

        try {
            library.returnBook(memberId, isbn);
            System.out.println("Book returned successfully.");
        } catch (MemberNotFoundException | BookNotFoundException e) {
            System.out.println("Could not return book: " + e.getMessage());
        }
    }

    private static void listMembers() {
        List<Member> allMembers = library.getAllMembers();
        if (allMembers.isEmpty()) {
            System.out.println("No members registered.");
            return;
        }
        for (Member m : allMembers) {
            System.out.println(m + " | Borrowed: " + m.getBorrowedCount());
        }
    }

    private static void viewTransactionLog() {
        List<Transaction> log = library.getTransactionLog();
        if (log.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        log.forEach(System.out::println);
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
