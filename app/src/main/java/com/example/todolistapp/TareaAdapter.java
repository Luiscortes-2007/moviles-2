package com.example.todolistapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private List<Tarea> listaTareas;

    public TareaAdapter(List<Tarea> listaTareas) {
        this.listaTareas = listaTareas;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);

        return new TareaViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {

        Tarea tareaActual = listaTareas.get(position);

        holder.tvTitulo.setText(
                "ID: " + tareaActual.getId() +
                        " - " + tareaActual.getTitulo()
        );

        holder.tvDescripcion.setText(
                tareaActual.getDescripcion() + " (Cant: " + tareaActual.getCantidad() + ")"
        );

        String estado = tareaActual.getEstado();
        holder.tvEstado.setText(estado);

        // Cambiar color según disponibilidad
        if (estado.equals("Disponible")) {
            holder.tvEstado.setTextColor(android.graphics.Color.parseColor("#03DAC6")); // Turquesa/Verde
        } else {
            holder.tvEstado.setTextColor(android.graphics.Color.parseColor("#FF5252")); // Rojo
        }

    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    public static class TareaViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo, tvDescripcion, tvEstado;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitulo = itemView.findViewById(R.id.tvItemTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvItemDescripcion);
            tvEstado = itemView.findViewById(R.id.tvItemEstado);

        }

    }

}