package com.library.api;

import com.library.exceptions.*;
import com.library.model.*;
import com.library.service.Library;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exposes the existing Library service (com.library.service.Library, and the
 * Book / Member / Transaction model classes) over a small REST API, so the
 * HTML/JS frontend can drive the real Java backend instead of a JS copy of it.
 *
 * No Spring, no Maven required - built entirely on the JDK's own
 * com.sun.net.httpserver package, so IntelliJ can compile and run it exactly
 * like the console Main.java.
 *
 * Endpoints:
 *   GET  /api/health
 *   GET  /api/books
 *   POST /api/books        { isbn, title, author, copies }
 *   GET  /api/members
 *   POST /api/members      { id, name, email }
 *   POST /api/issue        { memberId, isbn }
 *   POST /api/return       { memberId, isbn }
 *   GET  /api/transactions
 */
public class ApiServer {

    private static final Library library = new Library("City Central Library");
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        seedData();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/health", ApiServer::handleHealth);
        server.createContext("/api/books", ApiServer::handleBooks);
        server.createContext("/api/members", ApiServer::handleMembers);
        server.createContext("/api/issue", ApiServer::handleIssue);
        server.createContext("/api/return", ApiServer::handleReturn);
        server.createContext("/api/transactions", ApiServer::handleTransactions);
        server.setExecutor(null);
        server.start();

        System.out.println("Library API running at http://localhost:" + PORT);
        System.out.println("Open index.html in your browser to use it.");
    }

    private static void seedData() {
        library.addBook(new Book("ISBN001", "Clean Code", "Robert C. Martin", 3));
        library.addBook(new Book("ISBN002", "Effective Java", "Joshua Bloch", 2));
        library.addBook(new Book("ISBN003", "The Pragmatic Programmer", "Andrew Hunt", 1));
        library.registerMember(new Member("M001", "Asha Rao", "asha@example.com"));
        library.registerMember(new Member("M002", "Vikram Singh", "vikram@example.com"));
    }

    // ---------------- Handlers ----------------

    private static void handleHealth(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("library", library.getName());
        sendJson(ex, 200, body);
    }

    private static void handleBooks(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        String method = ex.getRequestMethod();

        if (method.equals("GET")) {
            List<Map<String, Object>> books = library.getAllBooks().stream()
                    .map(ApiServer::bookToMap)
                    .collect(Collectors.toList());
            sendJson(ex, 200, books);
            return;
        }

        if (method.equals("POST")) {
            Map<String, Object> req = Json.parseObject(readBody(ex));
            String isbn = Json.getString(req, "isbn");
            String title = Json.getString(req, "title");
            String author = Json.getString(req, "author");
            int copies = Json.getInt(req, "copies", 0);

            if (isEmpty(isbn) || isEmpty(title) || isEmpty(author) || copies < 1) {
                sendError(ex, 400, "isbn, title, author are required and copies must be at least 1.");
                return;
            }
            library.addBook(new Book(isbn, title, author, copies));
            sendJson(ex, 201, bookToMap(library.getAllBooks().stream()
                    .filter(b -> b.getIsbn().equals(isbn)).findFirst().orElseThrow()));
            return;
        }

        sendError(ex, 405, "Method not allowed.");
    }

    private static void handleMembers(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        String method = ex.getRequestMethod();

        if (method.equals("GET")) {
            List<Map<String, Object>> members = library.getAllMembers().stream()
                    .map(ApiServer::memberToMap)
                    .collect(Collectors.toList());
            sendJson(ex, 200, members);
            return;
        }

        if (method.equals("POST")) {
            Map<String, Object> req = Json.parseObject(readBody(ex));
            String id = Json.getString(req, "id");
            String name = Json.getString(req, "name");
            String email = Json.getString(req, "email");

            if (isEmpty(id) || isEmpty(name) || isEmpty(email)) {
                sendError(ex, 400, "id, name, and email are all required.");
                return;
            }
            boolean exists = library.getAllMembers().stream().anyMatch(m -> m.getId().equals(id));
            if (exists) {
                sendError(ex, 409, "A member with ID " + id + " already exists.");
                return;
            }
            Member member = new Member(id, name, email);
            library.registerMember(member);
            sendJson(ex, 201, memberToMap(member));
            return;
        }

        sendError(ex, 405, "Method not allowed.");
    }

    private static void handleIssue(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        if (!ex.getRequestMethod().equals("POST")) {
            sendError(ex, 405, "Method not allowed.");
            return;
        }
        Map<String, Object> req = Json.parseObject(readBody(ex));
        String memberId = Json.getString(req, "memberId");
        String isbn = Json.getString(req, "isbn");

        try {
            library.issueBook(memberId, isbn);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            sendJson(ex, 200, result);
        } catch (MemberNotFoundException | BookNotFoundException e) {
            sendError(ex, 404, e.getMessage());
        } catch (BookNotAvailableException | MaxBooksLimitException e) {
            sendError(ex, 409, e.getMessage());
        }
    }

    private static void handleReturn(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        if (!ex.getRequestMethod().equals("POST")) {
            sendError(ex, 405, "Method not allowed.");
            return;
        }
        Map<String, Object> req = Json.parseObject(readBody(ex));
        String memberId = Json.getString(req, "memberId");
        String isbn = Json.getString(req, "isbn");

        try {
            library.returnBook(memberId, isbn);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            sendJson(ex, 200, result);
        } catch (MemberNotFoundException | BookNotFoundException e) {
            sendError(ex, 404, e.getMessage());
        }
    }

    private static void handleTransactions(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        if (!ex.getRequestMethod().equals("GET")) {
            sendError(ex, 405, "Method not allowed.");
            return;
        }
        List<Map<String, Object>> log = library.getTransactionLog().stream()
                .map(ApiServer::transactionToMap)
                .collect(Collectors.toList());
        sendJson(ex, 200, log);
    }

    // ---------------- Model -> JSON-friendly Map ----------------

    private static Map<String, Object> bookToMap(Book b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isbn", b.getIsbn());
        m.put("title", b.getTitle());
        m.put("author", b.getAuthor());
        m.put("totalCopies", b.getTotalCopies());
        m.put("availableCopies", b.getAvailableCopies());
        return m;
    }

    private static Map<String, Object> memberToMap(Member m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        map.put("email", m.getEmail());
        map.put("borrowedIsbns", m.getBorrowedBooks().stream().map(Book::getIsbn).collect(Collectors.toList()));
        return map;
    }

    private static Map<String, Object> transactionToMap(Transaction t) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", t.getFormattedTimestamp());
        map.put("type", t.getType().name());
        map.put("memberName", t.getMember().getName());
        map.put("bookTitle", t.getBook().getTitle());
        return map;
    }

    // ---------------- HTTP plumbing ----------------

    /** Adds CORS headers and answers OPTIONS preflight requests. Returns true if the request was fully handled. */
    private static boolean preflight(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = Json.write(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange ex, int status, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        sendJson(ex, status, body);
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }
}
