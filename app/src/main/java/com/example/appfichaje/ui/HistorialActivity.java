package com.example.appfichaje.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appfichaje.R;
import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.modelos.FichajeHistorial;
import com.example.appfichaje.ui.adaptadores.HistorialAdapter;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialActivity extends AppCompatActivity {

    private HistorialAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        String empleadoId = getIntent().getStringExtra("EMPLEADO_ID");
        String empleadoNombre = getIntent().getStringExtra("EMPLEADO_NOMBRE");

        TextView tvNombre = findViewById(R.id.tvNombreEmpleado);
        tvNombre.setText("Fichajes de: " + empleadoNombre);

        RecyclerView rv = findViewById(R.id.rvHistorial);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistorialAdapter();
        rv.setAdapter(adapter);

        cargarHistorial(empleadoId);
    }

    private void cargarHistorial(String id) {
        ClienteApi.obtenerCliente(this).obtenerHistorialFichajes(id).enqueue(new Callback<List<FichajeHistorial>>() {
            @Override
            public void onResponse(Call<List<FichajeHistorial>> call, Response<List<FichajeHistorial>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setHistorial(response.body());
                } else {
                    Toast.makeText(HistorialActivity.this, "No se pudieron cargar los datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FichajeHistorial>> call, Throwable t) {
                Toast.makeText(HistorialActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}