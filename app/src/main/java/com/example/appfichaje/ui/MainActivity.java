package com.example.appfichaje.ui;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.example.appfichaje.viewmodel.MainViewModel;
import com.example.appfichaje.servicios.NotificacionWorker;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private static final int CODIGO_PERMISO_UBICACION = 100;
    private static final int CODIGO_PERMISO_NOTIFICACIONES = 101;
    private TextView tvStatus;
    private ProgressBar progressBar;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEntrada = findViewById(R.id.btnEntrada);
        Button btnSalida = findViewById(R.id.btnSalida);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        Button btnIncidencias = findViewById(R.id.btnIncidencias);
        Button btnResumen = findViewById(R.id.btnResumen);
        Button btnAdminMapa = findViewById(R.id.btnAdminMapa);
        Button btnAdminTrabajadores = findViewById(R.id.btnAdminTrabajadores);

        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBarMain);
        GestorSesion sesion = new GestorSesion(this);
        String rolUsuario = sesion.obtenerRol();

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.mensajeEstado.observe(this, mensaje -> {
            tvStatus.setText(mensaje);
        });

        mainViewModel.procesando.observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
            btnEntrada.setEnabled(!cargando);
            btnSalida.setEnabled(!cargando);
            if (btnCerrarSesion != null) btnCerrarSesion.setEnabled(!cargando);
        });

        mainViewModel.sesionCaducada.observe(this, caducada -> {
            if (caducada) {
                ejecutarCierreSesionLocal();
            }
        });

        mainViewModel.logoutCompletado.observe(this, completado -> {
            if (completado) {
                ejecutarCierreSesionLocal();
            }
        });

        btnEntrada.setOnClickListener(v -> verificarPermisosYFichar(true));
        btnSalida.setOnClickListener(v -> verificarPermisosYFichar(false));
        btnIncidencias.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IncidenciasActivity.class);
            startActivity(intent);
        });
        btnResumen.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ResumenActivity.class));
        });
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> mainViewModel.cerrarSesionApi());
        }

        if ("Administrador".equalsIgnoreCase(rolUsuario.trim())) {
            btnAdminMapa.setVisibility(View.VISIBLE);
            btnAdminTrabajadores.setVisibility(View.VISIBLE);

            btnAdminMapa.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MapaAdminActivity.class);
                startActivity(intent);
            });

            btnAdminTrabajadores.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ListaTrabajadoresActivity.class);
                startActivity(intent);
            });
        } else {
            btnAdminMapa.setVisibility(View.GONE);
        }

        inicializarNFC();
        mainViewModel.comprobarEstadoSesion();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, CODIGO_PERMISO_NOTIFICACIONES);
            }
        }

        PeriodicWorkRequest peticionTrabajo = new PeriodicWorkRequest.Builder(
                NotificacionWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "revisar_retrasos_fichaje",
                ExistingPeriodicWorkPolicy.KEEP,
                peticionTrabajo
        );
    }

    private void inicializarNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta NFC", Toast.LENGTH_LONG).show();
        } else {
            Intent nfcIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }
            pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, flags);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            procesarNFC(intent);
        }
    }

    private void procesarNFC(Intent intent) {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null && rawMessages.length > 0) {
            NdefMessage mensajeNdef = (NdefMessage) rawMessages[0];
            NdefRecord registro = mensajeNdef.getRecords()[0];

            byte[] payload = registro.getPayload();
            String textoNFC = "";
            try {
                int languageCodeLength = payload[0] & 0063;
                textoNFC = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (textoNFC.toLowerCase().contains("fichaje")) {
                Toast.makeText(this, "Etiqueta NFC detectada", Toast.LENGTH_SHORT).show();
                Boolean estaDentro = mainViewModel.estaDentro.getValue();
                boolean esEntrada = (estaDentro == null || !estaDentro);
                verificarPermisosYFichar(esEntrada);
            }
        }
        else {
            Toast.makeText(this, "Etiqueta NFC no válida para fichar", Toast.LENGTH_SHORT).show();
        }
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

    private void ejecutarCierreSesionLocal() {
        WorkManager.getInstance(this).cancelAllWork();

        GestorSesion sesion = new GestorSesion(this);
        sesion.cerrarSesion();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}