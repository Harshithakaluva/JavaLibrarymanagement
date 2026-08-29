package com.library.model;

public class Librarian extends Person {

    private String employeeId;

    public Librarian(String id, String name, String email, String employeeId) {
        super(id, name, email);
        this.employeeId = employeeId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    // Polymorphism: same method signature as Member.getRole(), different behavior
    @Override
    public String getRole() {
        return "Librarian";
    }
}
