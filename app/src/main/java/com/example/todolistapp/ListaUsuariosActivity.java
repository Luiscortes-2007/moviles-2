package com.example.todolistapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ListaUsuariosActivity extends AppCompatActivity implements UsuarioAdapter.OnUsuarioClickListener {

    private RecyclerView rvUsuarios;
    private UsuarioAdapter adapter;
    private List<Usuario> listaUsuarios;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_usuarios);

        rvUsuarios = findViewById(R.id.rvListaUsuarios);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        
        listaUsuarios = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        db.collection("users").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                listaUsuarios.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Usuario usuario = document.toObject(Usuario.class);
                    listaUsuarios.add(usuario);
                }
                adapter = new UsuarioAdapter(listaUsuarios, this);
                rvUsuarios.setAdapter(adapter);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onEditClick(Usuario usuario) {
        mostrarDialogoEditar(usuario);
    }

    @Override
    public void onDeleteClick(Usuario usuario) {
        String currentStatus = usuario.getStatus() != null ? usuario.getStatus() : "activo";
        
        if (currentStatus.equals("activo")) {
            // Lógica para Deshabilitar
            new AlertDialog.Builder(this)
                    .setTitle("Deshabilitar Usuario")
                    .setMessage("¿Deseas deshabilitar a " + usuario.getEmail() + "?\n\n" +
                            "El usuario no podrá ingresar a la app hasta que lo habilites de nuevo.")
                    .setPositiveButton("Deshabilitar", (dialog, which) -> {
                        db.collection("users").document(usuario.getUid()).update("status", "deshabilitado")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Usuario deshabilitado", Toast.LENGTH_SHORT).show();
                                    cargarUsuarios();
                                });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            // Lógica para Habilitar
            new AlertDialog.Builder(this)
                    .setTitle("Habilitar Usuario")
                    .setMessage("¿Deseas habilitar nuevamente a " + usuario.getEmail() + "?")
                    .setPositiveButton("Habilitar", (dialog, which) -> {
                        db.collection("users").document(usuario.getUid()).update("status", "activo")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Usuario habilitado con éxito", Toast.LENGTH_SHORT).show();
                                    cargarUsuarios();
                                });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }

    private void mostrarDialogoEditar(Usuario usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_editar_rol, null);
        Spinner spinner = view.findViewById(R.id.spinnerEditarRol);

        String[] roles = {"usuario_empleado", "usuario_jefe"};
        ArrayAdapter<String> adapterRol = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spinner.setAdapter(adapterRol);

        // Pre-seleccionar el rol actual
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(usuario.getRole())) {
                spinner.setSelection(i);
                break;
            }
        }

        builder.setView(view)
                .setTitle("Editar Rol de " + usuario.getEmail())
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoRol = spinner.getSelectedItem().toString();
                    db.collection("users").document(usuario.getUid()).update("role", nuevoRol)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Rol actualizado", Toast.LENGTH_SHORT).show();
                                cargarUsuarios();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}