package com.example.flowerking;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class SensorAlertWorker extends Worker {

    private static final String CHANNEL_ID =
            "flower_king_sensor_alerts";

    private static final String DEVICE_ID =
            "ESP32_PRINCIPAL";

    public SensorAlertWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {

        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();

        int userId = SessionManager.getUserId(context);

        if (userId == -1) {
            return Result.success();
        }

        DatabaseHelper db = new DatabaseHelper(context);

        // Comprobar qué cultivo tiene conectado el ESP32.

        int assignedCropId =
                db.getAssignedCropId(DEVICE_ID);

        if (assignedCropId == -1) {
            return Result.success();
        }

        // Obtener los cultivos del usuario.

        List<CropModel> cultivos =
                db.getCropsByUser(userId);

        CropModel cropEncontrado = null;

        for (CropModel crop : cultivos) {

            if (crop.getId() == assignedCropId) {

                cropEncontrado = crop;
                break;
            }
        }

        if (cropEncontrado == null) {
            return Result.success();
        }

        CropModel crop = cropEncontrado;

        SharedPreferences settings =
                context.getSharedPreferences(
                        "FlowerKingSettings",
                        Context.MODE_PRIVATE
                );

        SharedPreferences alertPrefs =
                context.getSharedPreferences(
                        "sensor_alerts",
                        Context.MODE_PRIVATE
                );

        // Respetar el interruptor de alertas existente.

        boolean alertasActivas =
                settings.getBoolean("ALERT_SOIL", true);

        if (!alertasActivas) {

            alertPrefs.edit().clear().apply();

            return Result.success();
        }

        double humedad =
                extraerNumero(crop.getMoisture());

        double temperatura =
                extraerNumero(crop.getSubstrateTemp());

        // =====================================
        // ALERTA DE HUMEDAD BAJA
        // =====================================

        String humedadKey =
                "humidity_" + crop.getId();

        boolean humedadBaja =
                humedad < crop.getMinMoisture();

        if (humedadBaja) {

            boolean alertaAnterior =
                    alertPrefs.getBoolean(humedadKey, false);

            if (!alertaAnterior) {

                String mensaje =
                        "El cultivo " + crop.getName() +
                                " registra " + humedad +
                                "% de humedad. El mínimo recomendado es " +
                                crop.getMinMoisture() +
                                "%. Revisa el riego.";

                enviarNotificacion(
                        context,
                        crop,
                        "¡Humedad baja!",
                        mensaje,
                        1
                );

                db.registerCropHistory(
                        crop.getId(),
                        "ALERTA",
                        "Humedad baja: " + humedad +
                                "%. Mínimo: " + crop.getMinMoisture() + "%"
                );

                alertPrefs.edit()
                        .putBoolean(humedadKey, true)
                        .apply();
            }

        } else {

            // Permitir una nueva alerta cuando se normalice.

            alertPrefs.edit()
                    .putBoolean(humedadKey, false)
                    .apply();
        }

        // =====================================
        // ALERTA DE TEMPERATURA
        // =====================================

        String temperaturaKey =
                "temperature_" + crop.getId();

        boolean temperaturaCritica =
                temperatura < crop.getMinTemp() ||
                        temperatura > crop.getMaxTemp();

        if (temperaturaCritica) {

            boolean alertaAnterior =
                    alertPrefs.getBoolean(
                            temperaturaKey,
                            false
                    );

            if (!alertaAnterior) {

                String mensaje =
                        "El cultivo " + crop.getName() +
                                " registra " + temperatura +
                                "°C. El rango permitido es de " +
                                crop.getMinTemp() + "°C a " +
                                crop.getMaxTemp() +
                                "°C. Revisa las condiciones ambientales.";

                enviarNotificacion(
                        context,
                        crop,
                        "¡Temperatura fuera de rango!",
                        mensaje,
                        2
                );

                db.registerCropHistory(
                        crop.getId(),
                        "ALERTA",
                        "Temperatura fuera de rango: " +
                                temperatura + "°C"
                );

                alertPrefs.edit()
                        .putBoolean(temperaturaKey, true)
                        .apply();
            }

        } else {

            alertPrefs.edit()
                    .putBoolean(temperaturaKey, false)
                    .apply();
        }

        return Result.success();
    }

    // Convertir valores como 24°C o 65% en números.

    private double extraerNumero(String valor) {

        try {

            if (valor == null) {
                return 0;
            }

            String limpio =
                    valor.replaceAll("[^0-9.]", "");

            return Double.parseDouble(limpio);

        } catch (Exception e) {

            return 0;
        }
    }

    // Crear y mostrar la notificación.

    private void enviarNotificacion(
            Context context,
            CropModel crop,
            String titulo,
            String mensaje,
            int tipo) {

        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        // Canal para Android 8 o superior.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Alertas de sensores",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Avisos de humedad y temperatura de los cultivos"
            );

            manager.createNotificationChannel(channel);
        }

        // Al pulsar la notificación abrirá el cultivo.

        Intent intent = new Intent(
                context,
                CropDetailActivity.class
        );

        intent.putExtra("EXTRA_CROP", crop);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        crop.getId() * 10 + tipo,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                );

        notification
                .setSmallIcon(
                        android.R.drawable.ic_dialog_alert
                )
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(
                        new NotificationCompat.BigTextStyle()
                                .bigText(mensaje)
                )
                .setPriority(
                        NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(
                crop.getId() * 10 + tipo,
                notification.build()
        );
    }
}