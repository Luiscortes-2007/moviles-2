package com.example.todolistapp;

public class Tarea {

    private int id;
    private String titulo;
    private String descripcion;
    private int cantidad;
    private String estado;

    public Tarea(int id, String titulo, String descripcion, int cantidad, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getEstado() {
        return estado;
    }

}