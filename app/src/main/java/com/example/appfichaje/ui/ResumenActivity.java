package com.example.appfichaje.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.appfichaje.R;
import com.example.appfichaje.viewmodel.ResumenViewModel;

import java.util.Calendar;
import java.util.Locale;

public class ResumenActivity extends AppCompatActivity {

    private ResumenViewModel viewModel;
    private Calendar calendarioActual;
    private TextView tvMesActual, tvHorasTrabajadas, tvHorasEsperadas, tvHorasExtra;
    private ProgressBar progressBar;
    private LinearLayout layoutDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        Button btnAnterior = findViewById(R.id.btnMesAnterior);
        Button btnSiguiente = findViewById(R.id.btnMesSiguiente);

        tvMesActual = findViewById(R.id.tvMesActual);
        tvHorasTrabajadas = findViewById(R.id.tvHorasTrabajadas);
        tvHorasEsperadas = findViewById(R.id.tvHorasEsperadas);
        tvHorasExtra = findViewById(R.id.tvHorasExtra);
        progressBar = findViewById(R.id.progressBarResumen);
        layoutDatos = findViewById(R.id.layoutDatos);

        viewModel = new ViewModelProvider(this).get(ResumenViewModel.class);
        calendarioActual = Calendar.getInstance();

        btnVolver.setOnClickListener(v -> finish());

        btnAnterior.setOnClickListener(v -> cambiarMes(-1));
        btnSiguiente.setOnClickListener(v -> cambiarMes(1));

        viewModel.cargando.observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
            layoutDatos.setVisibility(cargando ? View.INVISIBLE : View.VISIBLE);
        });

        viewModel.resumen.observe(this, resumen -> {
            tvHorasTrabajadas.setText(resumen.getHorasTrabajadas() + " h");
            tvHorasEsperadas.setText(resumen.getHorasEsperadas() + " h");
            tvHorasExtra.setText(resumen.getHorasExtra() + " h");
        });

        viewModel.mensajeError.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                tvHorasTrabajadas.setText("0.0 h");
                tvHorasEsperadas.setText("0.0 h");
                tvHorasExtra.setText("0.0 h");
            }
        });

        actualizarPeticion();
    }

    private void cambiarMes(int incremento) {
        calendarioActual.add(Calendar.MONTH, incremento);
        actualizarPeticion();
    }

    private void actualizarPeticion() {
        int year = calendarioActual.get(Calendar.YEAR);
        int month = calendarioActual.get(Calendar.MONTH) + 1;

        String mesFormat = String.format(Locale.getDefault(), "%04d-%02d", year, month);
        tvMesActual.setText(mesFormat);

        viewModel.cargarResumen(mesFormat);
    }
}