package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.todolistapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Botón Iniciar Sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            irAlMainActivity()
                        } else {
                            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Registrarse
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            guardarUsuarioEnFirestore(email)
                        } else {
                            Toast.makeText(this, "El usuario ya existe o error al registrar", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Completa los campos para registrarte", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Recuperar Contraseña
        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error al enviar correo", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Escribe tu correo arriba para recuperar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarUsuarioEnFirestore(email: String) {
        val uid = auth.currentUser?.uid ?: return
        val usuarioMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "fechaRegistro" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("usuarios").document(uid).set(usuarioMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                irAlMainActivity()
            }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            irAlMainActivity()
        }
    }

    private fun irAlMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}