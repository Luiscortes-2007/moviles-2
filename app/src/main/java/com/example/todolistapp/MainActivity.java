package com.example.todolistapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "tareas.db";

    // Instancia de Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText etId, etTitulo, etDescripcion, etCantidad;
    private CheckBox cbCompletada;
    private ImageButton btnGuardar, btnBuscar, btnEditar, btnEliminar;
    private Button btnVerTodas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etId = findViewById(R.id.etId);
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etCantidad = findViewById(R.id.etCantidad);
        cbCompletada = findViewById(R.id.cbCompletada);

        btnGuardar = findViewById(R.id.btnGuardar);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnVerTodas = findViewById(R.id.btnVerTodas);

        btnGuardar.setOnClickListener(view -> registrarProducto());
        btnBuscar.setOnClickListener(view -> buscarProducto());
        btnEditar.setOnClickListener(view -> editarProducto());
        btnEliminar.setOnClickListener(view -> eliminarProducto());
        btnVerTodas.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ListaTareasActivity.class)));
    }

    private void registrarProducto() {
        if (!validarCampos()) return;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

        ContentValues registro = new ContentValues();
        registro.put("id", Integer.parseInt(etId.getText().toString()));
        registro.put("titulo", etTitulo.getText().toString());
        registro.put("descripcion", etDescripcion.getText().toString());
        registro.put("cantidad", Integer.parseInt(etCantidad.getText().toString()));
        registro.put("estado", cbCompletada.isChecked() ? "Disponible" : "No disponible");

        long resultado = baseDeDatos.insert("tareas", null, registro);
        baseDeDatos.close();

        if (resultado != -1) {
            // Sincronización Nube
            Map<String, Object> data = new HashMap<>();
            data.put("titulo", etTitulo.getText().toString());
            data.put("descripcion", etDescripcion.getText().toString());
            data.put("cantidad", Integer.parseInt(etCantidad.getText().toString()));
            data.put("disponible", cbCompletada.isChecked());

            db.collection("inventario").document(etId.getText().toString()).set(data);

            limpiarCampos();
            Toast.makeText(this, "Producto registrado (Local + Nube)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "El ID ya existe", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarProducto() {
        String id = etId.getText().toString();
        if (id.isEmpty()) return;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase baseDeDatos = admin.getReadableDatabase();

        Cursor fila = baseDeDatos.rawQuery("SELECT titulo, descripcion, cantidad, estado FROM tareas WHERE id=" + id, null);

        if (fila.moveToFirst()) {
            etTitulo.setText(fila.getString(0));
            etDescripcion.setText(fila.getString(1));
            etCantidad.setText(fila.getString(2));
            cbCompletada.setChecked(fila.getString(3).equals("Disponible"));
        } else {
            Toast.makeText(this, "No existe producto con ese ID", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        }
        fila.close();
        baseDeDatos.close();
    }

    private void editarProducto() {
        if (!validarCampos()) return;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

        ContentValues registro = new ContentValues();
        registro.put("titulo", etTitulo.getText().toString());
        registro.put("descripcion", etDescripcion.getText().toString());
        registro.put("cantidad", Integer.parseInt(etCantidad.getText().toString()));
        registro.put("estado", cbCompletada.isChecked() ? "Disponible" : "No disponible");

        int cantidad = baseDeDatos.update("tareas", registro, "id=" + etId.getText().toString(), null);
        baseDeDatos.close();

        // Actualizar Nube
        db.collection("inventario").document(etId.getText().toString()).update(
                "titulo", etTitulo.getText().toString(),
                "descripcion", etDescripcion.getText().toString(),
                "cantidad", Integer.parseInt(etCantidad.getText().toString()),
                "disponible", cbCompletada.isChecked()
        );

        Toast.makeText(this, cantidad == 1 ? "Producto actualizado" : "No encontrado", Toast.LENGTH_SHORT).show();
        if (cantidad == 1) limpiarCampos();
    }

    private void eliminarProducto() {
        String id = etId.getText().toString();
        if (id.isEmpty()) return;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase baseDeDatos = admin.getWritableDatabase();
        int cantidad = baseDeDatos.delete("tareas", "id=" + id, null);
        baseDeDatos.close();

        // Eliminar en Nube
        db.collection("inventario").document(id).delete();

        Toast.makeText(this, cantidad == 1 ? "Producto eliminado" : "No encontrado", Toast.LENGTH_SHORT).show();
        if (cantidad == 1) limpiarCampos();
    }

    private boolean validarCampos() {
        if (etId.getText().toString().isEmpty() || etTitulo.getText().toString().isEmpty() ||
                etDescripcion.getText().toString().isEmpty() || etCantidad.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        etId.setText("");
        etTitulo.setText("");
        etDescripcion.setText("");
        etCantidad.setText("");
        cbCompletada.setChecked(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}