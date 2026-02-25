package com.example.appfichaje.servicios;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.appfichaje.R;
import com.example.appfichaje.api.ClienteApi;
import com.example.appfichaje.api.ServicioApi;
import com.example.appfichaje.modelos.RespuestaNotificacion;
import com.example.appfichaje.ui.MainActivity;

import retrofit2.Response;

public class NotificacionWorker extends Worker {
    private static final String CANAL_ID = "CANAL_AVISOS";

    public NotificacionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ServicioApi api = ClienteApi.obtenerCliente(getApplicationContext());
        try {
            // Llamada síncrona (execute) en vez de asíncrona (enqueue) porque estamos en un Worker en segundo plano
            Response<RespuestaNotificacion> response = api.comprobarNotificacionPendiente().execute();

            if (response.isSuccessful() && response.body() != null) {
                if (response.body().isMostrar()) {
                    mostrarNotificacionLocal(response.body().getTitulo(), response.body().getMensaje());
                }
            } else if (response.code() == 401) {
                // Si el token ha caducado, no reintentamos
                return Result.failure();
            }
        } catch (Exception e) {
            // Si no hay internet, reintentará más tarde
            return Result.retry();
        }
        return Result.success();
    }

    private void mostrarNotificacionLocal(String titulo, String mensaje) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(CANAL_ID, "Recordatorios de Fichaje", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(canal);
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CANAL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}