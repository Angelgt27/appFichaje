package com.example.appfichaje.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appfichaje.R;
import com.example.appfichaje.ui.adaptadores.IncidenciasAdapter;
import com.example.appfichaje.viewmodel.IncidenciasViewModel;

public class IncidenciasActivity extends AppCompatActivity {

    private IncidenciasViewModel viewModel;
    private IncidenciasAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidencias);

        progressBar = findViewById(R.id.progressBarIncidencias);
        RecyclerView rvIncidencias = findViewById(R.id.rvIncidencias);
        Button btnAnadir = findViewById(R.id.btnAnadirIncidencia);
        ImageButton btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(v -> finish());

        rvIncidencias.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IncidenciasAdapter();
        rvIncidencias.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(IncidenciasViewModel.class);

        viewModel.listaIncidencias.observe(this, incidencias -> {
            adapter.setIncidencias(incidencias);
            if(incidencias.size() > 0) rvIncidencias.smoothScrollToPosition(0);
        });

        viewModel.cargando.observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
            btnAnadir.setEnabled(!cargando);
        });

        viewModel.mensajeToast.observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            }
        });

        btnAnadir.setOnClickListener(v -> mostrarDialogoNuevaIncidencia());

        viewModel.cargarIncidencias();
    }

    private void mostrarDialogoNuevaIncidencia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nueva Incidencia");
        builder.setMessage("Describe la incidencia a continuación:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setLines(3);
        input.setMaxLines(5);
        input.setGravity(Gravity.TOP | Gravity.START);

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String texto = input.getText().toString().trim();
            if (!texto.isEmpty()) {
                viewModel.crearIncidencia(texto);
            } else {
                Toast.makeText(this, "La descripción no puede estar vacía", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}