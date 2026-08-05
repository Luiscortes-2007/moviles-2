package com.example.todolistapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CrearUsuarioActivity extends AppCompatActivity {

    private EditText etNuevoEmail, etNuevoPassword, etConfirmPassword;
    private Spinner spinnerRoles;
    private Button btnCrearUsuarioFinal, btnVolver;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);

        db = FirebaseFirestore.getInstance();

        etNuevoEmail = findViewById(R.id.etNuevoEmail);
        etNuevoPassword = findViewById(R.id.etNuevoPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerRoles = findViewById(R.id.spinnerRoles);
        btnCrearUsuarioFinal = findViewById(R.id.btnCrearUsuarioFinal);
        btnVolver = findViewById(R.id.btnVolver);

        // Opciones del selector de roles (Jefe o Empleado)
        String[] roles = {"usuario_empleado", "usuario_jefe"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerRoles.setAdapter(adapter);

        btnCrearUsuarioFinal.setOnClickListener(v -> validarYCrearUsuario());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void validarYCrearUsuario() {
        String email = etNuevoEmail.getText().toString().trim();
        String password = etNuevoPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String rolSeleccionado = spinnerRoles.getSelectedItem().toString();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación clave: comprobar si las contraseñas coinciden
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Creación del usuario en Firebase Auth
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        // Guardar datos y el rol en la colección "users" de Firestore
                        Map<String, Object> usuarioMap = new HashMap<>();
                        usuarioMap.put("uid", uid);
                        usuarioMap.put("email", email);
                        usuarioMap.put("role", rolSeleccionado);
                        usuarioMap.put("status", "activo");
                        usuarioMap.put("fechaRegistro", com.google.firebase.Timestamp.now());

                        db.collection("users").document(uid).set(usuarioMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Usuario creado con éxito (" + rolSeleccionado + ")", Toast.LENGTH_LONG).show();
                                    finish(); // Cierra esta pantalla y regresa
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al guardar el rol en Firestore", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        String errorMsg = "Error al registrar";
                        if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                            errorMsg = "Este correo ya está registrado. Para usarlo de nuevo, el admin debe borrarlo de Firebase.";
                        } else if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}