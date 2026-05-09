package com.example.todolistapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etId, etTitulo, etDescripcion;
    private CheckBox cbCompletada;

    private Button btnGuardar, btnBuscar, btnEditar, btnEliminar, btnVerTodas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etId = findViewById(R.id.etId);
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);

        cbCompletada = findViewById(R.id.cbCompletada);

        btnGuardar = findViewById(R.id.btnGuardar);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnVerTodas = findViewById(R.id.btnVerTodas);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarTarea();
            }
        });

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buscarTarea();
            }
        });

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editarTarea();
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminarTarea();
            }
        });

        btnVerTodas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        MainActivity.this,
                        ListaTareasActivity.class
                );

                startActivity(intent);

            }
        });

    }

    private void registrarTarea() {

        String id = etId.getText().toString();
        String titulo = etTitulo.getText().toString();
        String descripcion = etDescripcion.getText().toString();

        String estado;

        if (cbCompletada.isChecked()) {
            estado = "Completada";
        } else {
            estado = "Pendiente";
        }

        if (!id.isEmpty() && !titulo.isEmpty() && !descripcion.isEmpty()) {

            AdminSQLiteOpenHelper admin =
                    new AdminSQLiteOpenHelper(this,
                            "tareas.db",
                            null,
                            2);

            SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

            ContentValues registro = new ContentValues();

            registro.put("id", Integer.parseInt(id));
            registro.put("titulo", titulo);
            registro.put("descripcion", descripcion);
            registro.put("estado", estado);

            long resultado = baseDeDatos.insert("tareas", null, registro);

            baseDeDatos.close();

            if (resultado != -1) {

                limpiarCampos();

                Toast.makeText(this,
                        "Tarea registrada correctamente",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this,
                        "El ID ya existe",
                        Toast.LENGTH_SHORT).show();
            }

        } else {

            Toast.makeText(this,
                    "Debes llenar todos los campos",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void buscarTarea() {

        String id = etId.getText().toString();

        if (!id.isEmpty()) {

            AdminSQLiteOpenHelper admin =
                    new AdminSQLiteOpenHelper(this,
                            "tareas.db",
                            null,
                            2);

            SQLiteDatabase baseDeDatos = admin.getReadableDatabase();

            Cursor fila = baseDeDatos.rawQuery(
                    "SELECT titulo, descripcion, estado FROM tareas WHERE id='" + id + "'",
                    null
            );

            if (fila.moveToFirst()) {

                etTitulo.setText(fila.getString(0));
                etDescripcion.setText(fila.getString(1));

                String estado = fila.getString(2);

                cbCompletada.setChecked(estado.equals("Completada"));

                Toast.makeText(this,
                        "Tarea encontrada",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this,
                        "No existe una tarea con ese ID",
                        Toast.LENGTH_SHORT).show();

                limpiarCampos();
            }

            fila.close();
            baseDeDatos.close();

        } else {

            Toast.makeText(this,
                    "Ingresa el ID de la tarea",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void editarTarea() {

        String id = etId.getText().toString();
        String titulo = etTitulo.getText().toString();
        String descripcion = etDescripcion.getText().toString();

        String estado;

        if (cbCompletada.isChecked()) {
            estado = "Completada";
        } else {
            estado = "Pendiente";
        }

        if (!id.isEmpty() && !titulo.isEmpty() && !descripcion.isEmpty()) {

            AdminSQLiteOpenHelper admin =
                    new AdminSQLiteOpenHelper(this,
                            "tareas.db",
                            null,
                            2);

            SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

            ContentValues registro = new ContentValues();

            registro.put("titulo", titulo);
            registro.put("descripcion", descripcion);
            registro.put("estado", estado);

            int cantidad = baseDeDatos.update(
                    "tareas",
                    registro,
                    "id='" + id + "'",
                    null
            );

            baseDeDatos.close();

            if (cantidad == 1) {

                Toast.makeText(this,
                        "Tarea actualizada correctamente",
                        Toast.LENGTH_SHORT).show();

                limpiarCampos();

            } else {

                Toast.makeText(this,
                        "No se encontró la tarea",
                        Toast.LENGTH_SHORT).show();
            }

        } else {

            Toast.makeText(this,
                    "Debes llenar todos los campos",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void eliminarTarea() {

        String id = etId.getText().toString();

        if (!id.isEmpty()) {

            AdminSQLiteOpenHelper admin =
                    new AdminSQLiteOpenHelper(this,
                            "tareas.db",
                            null,
                            2);

            SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

            int cantidad = baseDeDatos.delete(
                    "tareas",
                    "id='" + id + "'",
                    null
            );

            baseDeDatos.close();

            if (cantidad == 1) {

                Toast.makeText(this,
                        "Tarea eliminada correctamente",
                        Toast.LENGTH_SHORT).show();

                limpiarCampos();

            } else {

                Toast.makeText(this,
                        "No existe la tarea",
                        Toast.LENGTH_SHORT).show();
            }

        } else {

            Toast.makeText(this,
                    "Ingresa el ID de la tarea",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void limpiarCampos() {

        etId.setText("");
        etTitulo.setText("");
        etDescripcion.setText("");

        cbCompletada.setChecked(false);

    }

}