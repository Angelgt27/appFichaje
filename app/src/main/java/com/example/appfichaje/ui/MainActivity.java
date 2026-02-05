package com.example.appfichaje.ui;

import android.Manifest;
import android.content.Intent; // Importar Intent
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
import com.example.appfichaje.datos.GestorSesion;
import com.example.appfichaje.ui.LoginActivity;
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
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion); // Referencia al botón

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
            btnCerrarSesion.setEnabled(!cargando);
        });

        // Listeners Fichaje
        btnEntrada.setOnClickListener(v -> verificarPermisosYFichar(true));
        btnSalida.setOnClickListener(v -> verificarPermisosYFichar(false));

        // Listener CERRAR SESIÓN (NUEVO)
        btnCerrarSesion.setOnClickListener(v -> {
            // 1. Borrar datos de sesión
            GestorSesion sesion = new GestorSesion(this);
            sesion.cerrarSesion();

            // 2. Ir al Login
            Intent intent = new Intent(this, LoginActivity.class);
            // Esto borra el historial para que no puedas volver atrás con el botón físico
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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
                Toast.makeText(this, "Permiso concedido.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Se necesita GPS para fichar.", Toast.LENGTH_LONG).show();
            }
        }
    }
}