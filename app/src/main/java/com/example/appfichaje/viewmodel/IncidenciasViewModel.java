package com.example.appfichaje.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.modelos.Incidencia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidenciasViewModel extends AndroidViewModel {
    public MutableLiveData<List<Incidencia>> listaIncidencias = new MutableLiveData<>();
    public MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    public MutableLiveData<String> mensajeToast = new MutableLiveData<>();

    public IncidenciasViewModel(@NonNull Application application) {
        super(application);
    }

    public void cargarIncidencias() {
        cargando.setValue(true);
        ClienteApi.obtenerCliente(getApplication()).obtenerIncidencias().enqueue(new Callback<List<Incidencia>>() {
            @Override
            public void onResponse(Call<List<Incidencia>> call, Response<List<Incidencia>> response) {
                cargando.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaIncidencias.setValue(response.body());
                } else {
                    mensajeToast.setValue("Error al cargar incidencias");
                }
            }

            @Override
            public void onFailure(Call<List<Incidencia>> call, Throwable t) {
                cargando.setValue(false);
                mensajeToast.setValue("Fallo de red: " + t.getMessage());
            }
        });
    }

    public void crearIncidencia(String descripcion) {
        cargando.setValue(true);
        Map<String, String> body = new HashMap<>();
        body.put("descripcion", descripcion);

        ClienteApi.obtenerCliente(getApplication()).registrarIncidencia(body).enqueue(new Callback<Incidencia>() {
            @Override
            public void onResponse(Call<Incidencia> call, Response<Incidencia> response) {
                cargando.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    mensajeToast.setValue("Incidencia registrada con éxito");
                    cargarIncidencias(); // Recargamos la lista para ver la nueva
                } else {
                    mensajeToast.setValue("Error al registrar incidencia");
                }
            }

            @Override
            public void onFailure(Call<Incidencia> call, Throwable t) {
                cargando.setValue(false);
                mensajeToast.setValue("Fallo de red al guardar: " + t.getMessage());
            }
        });
    }
}