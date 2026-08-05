package com.example.todolistapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ListaTareasActivity extends AppCompatActivity {

    private RecyclerView rvTareas;
    private TareaAdapter adaptador;
    private List<Tarea> listaTareas;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tareas);

        // Vinculación del componente visual
        rvTareas = findViewById(R.id.rvListaTareas);
        rvTareas.setLayoutManager(new LinearLayoutManager(this));

        // Consultar el rol del usuario antes de cargar la lista
        verificarRolYcargarTareas();
    }

    private void verificarRolYcargarTareas() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                String role = "usuario_normal";
                if (documentSnapshot.exists() && documentSnapshot.getString("role") != null) {
                    role = documentSnapshot.getString("role");
                }

                // Cargamos las tareas de SQLite pasando el rol obtenido
                cargarTareas(role);
            }).addOnFailureListener(e -> {
                // Si falla la red, por seguridad asumimos rol de usuario normal
                cargarTareas("usuario_normal");
            });
        } else {
            cargarTareas("usuario_normal");
        }
    }

    private void cargarTareas(String userRole) {
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

        // Configurar el adaptador con la lista cargada
        adaptador = new TareaAdapter(listaTareas);
        rvTareas.setAdapter(adaptador);
    }
}