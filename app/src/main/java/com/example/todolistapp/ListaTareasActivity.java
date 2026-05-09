package com.example.todolistapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

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

        rvTareas = findViewById(R.id.rvListaTareas);

        rvTareas.setLayoutManager(new LinearLayoutManager(this));

        cargarTareas();
    }

    private void cargarTareas() {

        listaTareas = new ArrayList<>();

        AdminSQLiteOpenHelper admin =
                new AdminSQLiteOpenHelper(this,
                        "tareas.db",
                        null,
                        2);

        SQLiteDatabase baseDeDatos = admin.getReadableDatabase();

        Cursor fila = baseDeDatos.rawQuery(
                "SELECT id, titulo, descripcion, estado FROM tareas",
                null
        );

        while (fila.moveToNext()) {

            int id = fila.getInt(0);
            String titulo = fila.getString(1);
            String descripcion = fila.getString(2);
            String estado = fila.getString(3);

            listaTareas.add(
                    new Tarea(id, titulo, descripcion, estado)
            );
        }

        fila.close();
        baseDeDatos.close();

        adaptador = new TareaAdapter(listaTareas);

        rvTareas.setAdapter(adaptador);
    }
}