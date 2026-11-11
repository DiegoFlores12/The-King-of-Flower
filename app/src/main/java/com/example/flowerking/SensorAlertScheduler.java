package com.example.flowerking;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class SensorAlertScheduler {

    public static void programar(Context context) {

        Constraints constraints =
                new Constraints.Builder()
                        .setRequiredNetworkType(
                                NetworkType.CONNECTED
                        )
                        .build();

        PeriodicWorkRequest trabajo =
                new PeriodicWorkRequest.Builder(
                        SensorAlertWorker.class,
                        15,
                        TimeUnit.MINUTES
                )
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        "FLOWER_KING_SENSOR_ALERTS",
                        ExistingPeriodicWorkPolicy.KEEP,
                        trabajo
                );
    }
}