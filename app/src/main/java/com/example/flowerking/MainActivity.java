package com.example.flowerking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvActiveSummary, tvEmptyHomeCrops;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvHomeCrops;
    private MaterialButton btnQuickCrops, btnQuickIrrigation;

    private CropAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<CropModel> listaCultivos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        configurarNotificaciones();

        tvWelcome = findViewById(R.id.tvWelcome);
        tvActiveSummary = findViewById(R.id.tvActiveSummary);
        tvEmptyHomeCrops = findViewById(R.id.tvEmptyHomeCrops);
        rvHomeCrops = findViewById(R.id.rvHomeCrops);
        btnQuickCrops = findViewById(R.id.btnQuickCrops);
        btnQuickIrrigation = findViewById(R.id.btnQuickIrrigation);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        rvHomeCrops.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CropAdapter(new ArrayList<>(), crop -> {
            Intent intent = new Intent(MainActivity.this, CropDetailActivity.class);
            intent.putExtra("EXTRA_CROP", crop);
            startActivity(intent);
        });
        rvHomeCrops.setAdapter(adapter);

        btnQuickCrops.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CropsActivity.class);
            startActivity(intent);
        });

        btnQuickIrrigation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IrrigationActivity.class);
            startActivity(intent);
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_plants) {
                Intent intent = new Intent(MainActivity.this, CropsActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_irrigation) {
                Intent intent = new Intent(MainActivity.this, IrrigationActivity.class);
                startActivity(intent);
                return true;
            } else if (bottomNavigation.getMenu().size() > 3 && id == bottomNavigation.getMenu().getItem(3).getItemId()) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        String username = SessionManager.getUsername(this);
        int userId = SessionManager.getUserId(this);

        tvWelcome.setText("Hola, " + username);

        new Thread(() -> {
            List<CropModel> cultivos = (userId != -1) ? dbHelper.getCropsByUser(userId) : dbHelper.getAllCrops();

            runOnUiThread(() -> {
                listaCultivos = cultivos;

                if (listaCultivos.isEmpty()) {
                    tvActiveSummary.setText("• No tienes cultivos registrados");
                    tvEmptyHomeCrops.setVisibility(View.VISIBLE);
                    rvHomeCrops.setVisibility(View.GONE);
                } else {
                    int requierenRiego = 0;
                    for (CropModel c : listaCultivos) {
                        if ("Riego requerido".equalsIgnoreCase(c.getStatus())) {
                            requierenRiego++;
                        }
                    }

                    if (requierenRiego > 0) {
                        tvActiveSummary.setText("• " + requierenRiego + " cultivo(s) requieren atención hoy");
                    } else {
                        tvActiveSummary.setText("• " + listaCultivos.size() + " cultivo(s) en estado óptimo");
                    }

                    tvEmptyHomeCrops.setVisibility(View.GONE);
                    rvHomeCrops.setVisibility(View.VISIBLE);
                    adapter.updateList(listaCultivos);
                }
            });
        }).start();
    }
    private void configurarNotificaciones() {

        // Solo activar las revisiones si hay una sesión iniciada.

        if (SessionManager.getUserId(this) == -1) {
            return;
        }

        // Programar la revisión de sensores.

        SensorAlertScheduler.programar(this);

        // Solicitar permiso de Android 13 o superior.

        if (Build.VERSION.SDK_INT >= 33) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        1001
                );
            }
        }
    }

}