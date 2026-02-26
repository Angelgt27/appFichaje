package com.example.appfichaje.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.modelos.ResumenMensual;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResumenViewModel extends AndroidViewModel {
    public MutableLiveData<ResumenMensual> resumen = new MutableLiveData<>();
    public MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    public MutableLiveData<String> mensajeError = new MutableLiveData<>();

    public ResumenViewModel(@NonNull Application application) {
        super(application);
    }

    public void cargarResumen(String mesFormat) {
        cargando.setValue(true);
        ClienteApi.obtenerCliente(getApplication()).obtenerResumenMensual(mesFormat).enqueue(new Callback<ResumenMensual>() {
            @Override
            public void onResponse(Call<ResumenMensual> call, Response<ResumenMensual> response) {
                cargando.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    resumen.setValue(response.body());
                } else {
                    mensajeError.setValue("No hay datos para este mes");
                }
            }

            @Override
            public void onFailure(Call<ResumenMensual> call, Throwable t) {
                cargando.setValue(false);
                mensajeError.setValue("Error de conexión: " + t.getMessage());
            }
        });
    }
}