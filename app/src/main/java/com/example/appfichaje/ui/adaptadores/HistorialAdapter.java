package com.example.appfichaje.ui.adaptadores;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appfichaje.R;
import com.example.appfichaje.modelos.FichajeHistorial;
import java.util.ArrayList;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {
    private List<FichajeHistorial> lista = new ArrayList<>();

    public void setHistorial(List<FichajeHistorial> historial) {
        this.lista = historial;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CAMBIO AQUÍ: Inflamos nuestro nuevo layout de tarjeta profesional
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FichajeHistorial f = lista.get(position);

        // Enlazamos los datos
        holder.tvFecha.setText("Fecha: " + f.getFecha());
        holder.tvEntrada.setText(f.getHoraEntrada());

        String salida = f.getHoraSalida() != null ? f.getHoraSalida() : "Sin cerrar";
        holder.tvSalida.setText(salida);

        // Lógica de color si no han fichado salida
        if ("Sin cerrar".equals(salida)) {
            holder.tvSalida.setTextColor(Color.RED);
        } else {
            holder.tvSalida.setTextColor(Color.parseColor("#333333")); // Gris oscuro por defecto
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // CAMBIO AQUÍ: Usamos los IDs correctos de item_historial.xml
        TextView tvFecha, tvEntrada, tvSalida;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFechaItem);
            tvEntrada = itemView.findViewById(R.id.tvEntradaItem);
            tvSalida = itemView.findViewById(R.id.tvSalidaItem);
        }
    }
}