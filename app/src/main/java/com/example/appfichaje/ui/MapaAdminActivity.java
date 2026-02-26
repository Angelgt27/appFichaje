package com.example.appfichaje.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appfichaje.R;
import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.api.ServicioApi;
import com.example.appfichaje.modelos.EmpresaAdmin;
import com.example.appfichaje.modelos.RespuestaApi;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaAdminActivity extends AppCompatActivity {

    private MapView map;
    private Polygon circuloRadio;
    private GeoPoint centroEmpresa;
    private int radioActual = 100;

    private EditText etRadioActual;
    private SeekBar seekBarRadio;
    private Button btnGuardar, btnMenos, btnMas;
    private ServicioApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_mapa_admin);

        map = findViewById(R.id.mapaOSM);
        etRadioActual = findViewById(R.id.etRadioActual);
        seekBarRadio = findViewById(R.id.seekBarRadio);
        btnGuardar = findViewById(R.id.btnGuardarRadio);
        btnMenos = findViewById(R.id.btnMenosRadio);
        btnMas = findViewById(R.id.btnMasRadio);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        api = ClienteApi.obtenerCliente(this);

        desbloquearControles(false);
        cargarDatosEmpresa();

        seekBarRadio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    sincronizarRadio(progress, false, true);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        etRadioActual.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (etRadioActual.hasFocus() && s.length() > 0) {
                    try {
                        int valorTecleado = Integer.parseInt(s.toString());
                        sincronizarRadio(valorTecleado, true, false);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        });

        btnMenos.setOnClickListener(v -> sincronizarRadio(radioActual - 5, true, true));
        btnMas.setOnClickListener(v -> sincronizarRadio(radioActual + 5, true, true));

        btnGuardar.setOnClickListener(v -> guardarNuevoRadio());
    }

    private void sincronizarRadio(int nuevoValor, boolean actualizarBarra, boolean actualizarTexto) {
        if (nuevoValor < 10) nuevoValor = 10;
        if (nuevoValor > 1000) nuevoValor = 1000;

        radioActual = nuevoValor;

        if (actualizarBarra) {
            seekBarRadio.setProgress(radioActual);
        }
        if (actualizarTexto) {
            etRadioActual.setText(String.valueOf(radioActual));
            etRadioActual.setSelection(etRadioActual.getText().length());
        }

        dibujarCirculo();
    }

    private void desbloquearControles(boolean habilitar) {
        seekBarRadio.setEnabled(habilitar);
        btnGuardar.setEnabled(habilitar);
        etRadioActual.setEnabled(habilitar);
        btnMenos.setEnabled(habilitar);
        btnMas.setEnabled(habilitar);
        if(!habilitar) etRadioActual.setText("...");
    }

    private void cargarDatosEmpresa() {
        api.obtenerEmpresaAdmin().enqueue(new Callback<EmpresaAdmin>() {
            @Override
            public void onResponse(Call<EmpresaAdmin> call, Response<EmpresaAdmin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmpresaAdmin empresa = response.body();

                    centroEmpresa = new GeoPoint(empresa.getLatitud(), empresa.getLongitud());

                    map.post(() -> {
                        map.setExpectedCenter(centroEmpresa);
                        IMapController mapController = map.getController();
                        mapController.setZoom(17.0);
                        mapController.setCenter(centroEmpresa);
                    });

                    Marker marcador = new Marker(map);
                    marcador.setPosition(centroEmpresa);
                    marcador.setTitle("Nuestra Empresa");
                    marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(marcador);

                    desbloquearControles(true);
                    sincronizarRadio(empresa.getRadio(), true, true);

                } else {
                    Toast.makeText(MapaAdminActivity.this, "Error de permisos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EmpresaAdmin> call, Throwable t) {
                Toast.makeText(MapaAdminActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dibujarCirculo() {
        if (centroEmpresa == null) return;

        if (circuloRadio != null) {
            map.getOverlays().remove(circuloRadio);
        }

        circuloRadio = new Polygon(map);
        circuloRadio.setPoints(Polygon.pointsAsCircle(centroEmpresa, (double) radioActual));

        circuloRadio.setFillColor(Color.argb(51, 51, 136, 255));
        circuloRadio.setStrokeColor(Color.rgb(51, 136, 255));
        circuloRadio.setStrokeWidth(4.0f);

        map.getOverlays().add(circuloRadio);
        map.invalidate();
    }

    private void guardarNuevoRadio() {
        desbloquearControles(false);
        btnGuardar.setText("Guardando...");

        Map<String, String> body = new HashMap<>();
        body.put("radio", String.valueOf(radioActual));

        api.actualizarRadioEmpresa(body).enqueue(new Callback<RespuestaApi>() {
            @Override
            public void onResponse(Call<RespuestaApi> call, Response<RespuestaApi> response) {
                desbloquearControles(true);
                btnGuardar.setText("Guardar Nuevo Radio");

                if (response.isSuccessful()) {
                    Toast.makeText(MapaAdminActivity.this, "Radio actualizado a " + radioActual + "m", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapaAdminActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaApi> call, Throwable t) {
                desbloquearControles(true);
                btnGuardar.setText("Guardar Nuevo Radio");
                Toast.makeText(MapaAdminActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() { super.onResume(); if (map != null) map.onResume(); }

    @Override
    public void onPause() { super.onPause(); if (map != null) map.onPause(); }
}