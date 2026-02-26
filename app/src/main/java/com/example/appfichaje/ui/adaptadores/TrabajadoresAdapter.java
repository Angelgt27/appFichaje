package com.example.appfichaje.ui.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appfichaje.R;
import com.example.appfichaje.modelos.TrabajadorItem;
import java.util.ArrayList;
import java.util.List;

public class TrabajadoresAdapter extends RecyclerView.Adapter<TrabajadoresAdapter.ViewHolder> {
    private List<TrabajadorItem> lista = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TrabajadorItem item);
    }

    public TrabajadoresAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTrabajadores(List<TrabajadorItem> trabajadores) {
        this.lista = trabajadores;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CAMBIO AQUÍ: Inflamos nuestro nuevo layout de tarjeta
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trabajador, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrabajadorItem t = lista.get(position);
        holder.tvNombre.setText(t.getNombre());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(t));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // CAMBIO AQUÍ: Enlazamos con el ID correcto de item_trabajador.xml
            tvNombre = itemView.findViewById(R.id.tvNombreItem);
        }
    }
}