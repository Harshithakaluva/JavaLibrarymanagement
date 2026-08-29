package com.library.model;

/**
 * Abstract class: cannot be instantiated directly. Holds fields/behavior
 * common to every person in the system (Member, Librarian).
 */
public abstract class Person {
    // Encapsulation: fields are private, accessed only via getters/setters
    private final String id;
    private String name;
    private String email;

    public Person(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Abstract method: every subclass MUST provide its own version (polymorphism)
    public abstract String getRole();

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s", getRole(), name, id, email);
    }
}
