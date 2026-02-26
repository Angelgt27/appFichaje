package com.example.appfichaje.ui.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appfichaje.R;
import com.example.appfichaje.modelos.Incidencia;

import java.util.ArrayList;
import java.util.List;

public class IncidenciasAdapter extends RecyclerView.Adapter<IncidenciasAdapter.ViewHolder> {
    private List<Incidencia> incidencias = new ArrayList<>();

    public void setIncidencias(List<Incidencia> nuevasIncidencias) {
        this.incidencias = nuevasIncidencias;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Incidencia inc = incidencias.get(position);
        holder.tvFechaHora.setText(inc.getFecha() + " - " + inc.getHora());
        holder.tvDescripcion.setText(inc.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return incidencias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFechaHora, tvDescripcion;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
        }
    }
}