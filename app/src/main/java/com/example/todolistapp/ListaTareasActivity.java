package com.example.todolistapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ListaTareasActivity extends AppCompatActivity {

    private RecyclerView rvTareas;
    private TareaAdapter adaptador;
    private List<Tarea> listaTareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tareas);

        // Vinculación del componente visual
        rvTareas = findViewById(R.id.rvListaTareas);
        rvTareas.setLayoutManager(new LinearLayoutManager(this));

        // Cargar los datos desde SQLite
        cargarTareas();
    }

    private void cargarTareas() {
        listaTareas = new ArrayList<>();

        try {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(
                    this,
                    "tareas.db",
                    null,
                    3
            );

            SQLiteDatabase baseDeDatos = admin.getReadableDatabase();

            Cursor fila = baseDeDatos.rawQuery(
                    "SELECT id, titulo, descripcion, cantidad, estado FROM tareas",
                    null
            );

            // Obtener los índices de las columnas de forma segura
            int idIndex = fila.getColumnIndex("id");
            int tituloIndex = fila.getColumnIndex("titulo");
            int descripcionIndex = fila.getColumnIndex("descripcion");
            int cantidadIndex = fila.getColumnIndex("cantidad");
            int estadoIndex = fila.getColumnIndex("estado");

            // Validar que las columnas existan en la tabla antes de leer
            if (idIndex != -1 && tituloIndex != -1 && descripcionIndex != -1 && cantidadIndex != -1 && estadoIndex != -1) {
                while (fila.moveToNext()) {
                    int id = fila.getInt(idIndex);
                    String titulo = fila.getString(tituloIndex);
                    String descripcion = fila.getString(descripcionIndex);
                    int cantidad = fila.getInt(cantidadIndex);
                    String estado = fila.getString(estadoIndex);

                    listaTareas.add(new Tarea(id, titulo, descripcion, cantidad, estado));
                }
            }

            fila.close();
            baseDeDatos.close();

        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar tareas de SQLite: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Configurar el adaptador con la lista (vacía o llena)
        adaptador = new TareaAdapter(listaTareas);
        rvTareas.setAdapter(adaptador);
    }
}