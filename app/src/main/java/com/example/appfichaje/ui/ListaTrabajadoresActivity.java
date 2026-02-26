package com.example.appfichaje.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appfichaje.R;
import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.modelos.TrabajadorItem;
import com.example.appfichaje.ui.adaptadores.TrabajadoresAdapter;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaTrabajadoresActivity extends AppCompatActivity {

    private TrabajadoresAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_trabajadores);

        RecyclerView rv = findViewById(R.id.rvTrabajadores);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Al tocar un empleado, enviamos su ID y su Nombre a la siguiente pantalla
        adapter = new TrabajadoresAdapter(trabajador -> {
            Intent intent = new Intent(this, HistorialActivity.class);
            intent.putExtra("EMPLEADO_ID", String.valueOf(trabajador.getId()));
            intent.putExtra("EMPLEADO_NOMBRE", trabajador.getNombre());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        cargarTrabajadores();
    }

    private void cargarTrabajadores() {
        ClienteApi.obtenerCliente(this).obtenerTrabajadoresAdmin().enqueue(new Callback<List<TrabajadorItem>>() {
            @Override
            public void onResponse(Call<List<TrabajadorItem>> call, Response<List<TrabajadorItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setTrabajadores(response.body());
                } else {
                    Toast.makeText(ListaTrabajadoresActivity.this, "Error de permisos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TrabajadorItem>> call, Throwable t) {
                Toast.makeText(ListaTrabajadoresActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}