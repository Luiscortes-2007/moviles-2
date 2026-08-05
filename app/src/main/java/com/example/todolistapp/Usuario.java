package com.example.todolistapp;

public class Usuario {
    private String email;
    private String role;
    private String uid;
    private String status; // "activo" o "deshabilitado"

    public Usuario() {} // Requerido para Firestore

    public Usuario(String email, String role, String uid, String status) {
        this.email = email;
        this.role = role;
        this.uid = uid;
        this.status = status;
    }

    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getUid() { return uid; }
    public String getStatus() { return status; }
}