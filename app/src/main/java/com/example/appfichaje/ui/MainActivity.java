package com.example.appfichaje.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.appfichaje.R;
import com.example.appfichaje.viewmodel.MainViewModel;


public class MainActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private static final int CODIGO_PERMISO_UBICACION = 100;
    private TextView tvStatus;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEntrada = findViewById(R.id.btnEntrada);
        Button btnSalida = findViewById(R.id.btnSalida);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBarMain);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Observadores
        mainViewModel.mensajeEstado.observe(this, mensaje -> {
            tvStatus.setText(mensaje);
        });

        mainViewModel.procesando.observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
            btnEntrada.setEnabled(!cargando);
            btnSalida.setEnabled(!cargando);
        });

        // Listeners
        btnEntrada.setOnClickListener(v -> verificarPermisosYFichar(true));
        btnSalida.setOnClickListener(v -> verificarPermisosYFichar(false));
    }

    private void verificarPermisosYFichar(boolean esEntrada) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PERMISO_UBICACION);
        } else {
            mainViewModel.realizarFichaje(esEntrada);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISO_UBICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido. Pulse el botón de nuevo.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Se necesita GPS para fichar.", Toast.LENGTH_LONG).show();
            }
        }
    }
}