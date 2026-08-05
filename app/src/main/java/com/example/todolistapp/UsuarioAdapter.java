package com.example.todolistapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<Usuario> listaUsuarios;
    private OnUsuarioClickListener listener;

    public interface OnUsuarioClickListener {
        void onEditClick(Usuario usuario);
        void onDeleteClick(Usuario usuario);
    }

    public UsuarioAdapter(List<Usuario> listaUsuarios, OnUsuarioClickListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        holder.tvEmail.setText(usuario.getEmail());
        
        String status = usuario.getStatus() != null ? usuario.getStatus() : "activo";
        String roleText = usuario.getRole() != null ? usuario.getRole() : "usuario_empleado";
        
        // Traducir roles para mostrar
        if (roleText.equals("usuario_jefe") || roleText.equals("super_admin")) roleText = "Jefe";
        else if (roleText.equals("usuario_empleado") || roleText.equals("usuario_normal")) roleText = "Empleado";
        
        holder.tvRol.setText("Rol: " + roleText + " (" + status + ")");

        // Limpiar cualquier tinte previo de los iconos para que se vean normales
        holder.btnEdit.setColorFilter(null);
        holder.btnDelete.setColorFilter(null);

        if (status.equals("deshabilitado")) {
            holder.tvEmail.setTextColor(Color.LTGRAY);
            holder.btnDelete.setImageResource(android.R.drawable.ic_menu_revert); // Icono de reactivar de sistema
            holder.btnDelete.setColorFilter(Color.parseColor("#38B2AC")); // Turquesa suave para el de reactivar
        } else {
            // Color de texto oscuro para que se vea sobre el fondo claro
            holder.tvEmail.setTextColor(Color.parseColor("#2D3748")); 
            holder.btnDelete.setImageResource(R.drawable.delete); // Icono original
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(usuario));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(usuario));
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvRol;
        ImageButton btnEdit, btnDelete;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvUsuarioEmail);
            tvRol = itemView.findViewById(R.id.tvUsuarioRol);
            btnEdit = itemView.findViewById(R.id.btnEditarUsuario);
            btnDelete = itemView.findViewById(R.id.btnEliminarUsuario);
        }
    }
}