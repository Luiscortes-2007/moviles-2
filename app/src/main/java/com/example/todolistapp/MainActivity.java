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

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText etId, etTitulo, etDescripcion, etCantidad;
    private CheckBox cbCompletada;
    private ImageButton btnGuardar, btnBuscar, btnEditar, btnEliminar;
    private Button btnVerTodas, btnVerUsuarios;

    private String userRole = "usuario_empleado";

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
        btnVerUsuarios = findViewById(R.id.btnVerUsuarios);

        btnGuardar.setOnClickListener(view -> registrarProducto());
        btnBuscar.setOnClickListener(view -> buscarProducto());
        btnEditar.setOnClickListener(view -> editarProducto());
        btnEliminar.setOnClickListener(view -> eliminarProducto());
        btnVerTodas.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ListaTareasActivity.class)));

        if (btnVerUsuarios != null) {
            btnVerUsuarios.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, ListaUsuariosActivity.class);
                startActivity(intent);
            });
        }

        verificarRolUsuario();
    }

    private void verificarRolUsuario() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid != null) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String role = documentSnapshot.getString("role");
                        String status = documentSnapshot.getString("status");

                        if (documentSnapshot.exists() && !"deshabilitado".equals(status)) {
                            if (role != null) userRole = role;
                            
                            // Configuración según el rol (Jefe o Empleado)
                            // Aceptamos los nombres viejos para no perder acceso
                            if (userRole.equals("usuario_empleado") || userRole.equals("usuario_normal")) {
                                btnGuardar.setVisibility(View.GONE);
                                btnEditar.setVisibility(View.GONE);
                                btnEliminar.setVisibility(View.GONE);
                                if (btnVerUsuarios != null) btnVerUsuarios.setVisibility(View.GONE);
                                Toast.makeText(this, "Sesión: Empleado (Solo lectura)", Toast.LENGTH_SHORT).show();
                            } else if (userRole.equals("usuario_jefe") || userRole.equals("super_admin") || userRole.equals("admin")) {
                                // El Jefe tiene acceso completo
                                if (btnVerUsuarios != null) btnVerUsuarios.setVisibility(View.VISIBLE);
                                Toast.makeText(this, "Sesión: Jefe (Control total)", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // SI EL USUARIO HA SIDO DESHABILITADO
                            Toast.makeText(this, "Tu cuenta ha sido deshabilitada.", Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al verificar permisos", Toast.LENGTH_SHORT).show();
                    });
        }
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
            Map<String, Object> data = new HashMap<>();
            data.put("titulo", etTitulo.getText().toString());
            data.put("descripcion", etDescripcion.getText().toString());
            data.put("cantidad", Integer.parseInt(etCantidad.getText().toString()));
            data.put("disponible", cbCompletada.isChecked());
            db.collection("inventario").document(etId.getText().toString()).set(data);

            limpiarCampos();
            Toast.makeText(this, "Producto registrado", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "No existe", Toast.LENGTH_SHORT).show();
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
        baseDeDatos.update("tareas", registro, "id=" + etId.getText().toString(), null);
        baseDeDatos.close();
        Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
    }

    private void eliminarProducto() {
        String id = etId.getText().toString();
        if (id.isEmpty()) return;
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase baseDeDatos = admin.getWritableDatabase();
        baseDeDatos.delete("tareas", "id=" + id, null);
        baseDeDatos.close();
        db.collection("inventario").document(id).delete();
        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
        limpiarCampos();
    }

    private boolean validarCampos() {
        return !etId.getText().toString().isEmpty() && !etTitulo.getText().toString().isEmpty();
    }

    private void limpiarCampos() {
        etId.setText(""); etTitulo.setText(""); etDescripcion.setText(""); etCantidad.setText(""); cbCompletada.setChecked(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}