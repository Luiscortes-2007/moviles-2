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
                            verificarAccesoYEntrar()
                        } else {
                            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Registrarse - Redirige a la ventana de registro
        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, CrearUsuarioActivity::class.java)
            startActivity(intent)
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

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            verificarAccesoYEntrar()
        }
    }

    private fun verificarAccesoYEntrar() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val status = document.getString("status")
                if (document.exists() && status != "deshabilitado") {
                    // El usuario está habilitado
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // El usuario fue deshabilitado
                    auth.signOut()
                    Toast.makeText(this, "Usuario deshabilitado. Comunícate con el administrador.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                auth.signOut()
                Toast.makeText(this, "Error de conexión al verificar cuenta", Toast.LENGTH_SHORT).show()
            }
    }
}