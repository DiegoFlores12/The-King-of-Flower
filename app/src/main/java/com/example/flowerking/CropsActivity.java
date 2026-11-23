package com.example.flowerking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CropsActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MS = 6000L; // Actualiza cada 6 segundos

    private RecyclerView rvCrops;
    private BottomNavigationView bottomNav;
    private DatabaseHelper dbHelper;
    private CropAdapter adapter;
    private List<CropModel> listaCultivos = new ArrayList<>();

    private final Handler liveUpdateHandler = new Handler(Looper.getMainLooper());
    private boolean isUpdating = false;

    private final Runnable liveUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            cargarCultivosEnSegundoPlano(true);
            liveUpdateHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crops);

        dbHelper = new DatabaseHelper(this);

        rvCrops = findViewById(R.id.rvCrops);
        bottomNav = findViewById(R.id.bottomNavigationCrops);
        bottomNav.setSelectedItemId(R.id.nav_plants);

        rvCrops.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CropAdapter(new ArrayList<>(), crop -> {
            Intent intent = new Intent(CropsActivity.this, CropDetailActivity.class);
            intent.putExtra("EXTRA_CROP", crop);
            startActivity(intent);
        });
        rvCrops.setAdapter(adapter);

        FloatingActionButton fabAddCrop = findViewById(R.id.fabAddCrop);
        fabAddCrop.setOnClickListener(v -> {
            Intent intent = new Intent(CropsActivity.this, AddCropActivity.class);
            startActivity(intent);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_plants) {
                return true;
            } else if (id == R.id.nav_irrigation) {
                Intent intent = new Intent(CropsActivity.this, IrrigationActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (bottomNav.getMenu().size() > 3 && id == bottomNav.getMenu().getItem(3).getItemId()) {
                Intent intent = new Intent(CropsActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_plants);
        }
        cargarCultivosEnSegundoPlano(false);
        liveUpdateHandler.removeCallbacks(liveUpdateRunnable);
        liveUpdateHandler.postDelayed(liveUpdateRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        liveUpdateHandler.removeCallbacks(liveUpdateRunnable);
    }

    private void cargarCultivosEnSegundoPlano(boolean ejecutarSimulacion) {
        if (isUpdating) return;
        isUpdating = true;

        int userId = SessionManager.getUserId(this);

        new Thread(() -> {
            try {
                if (ejecutarSimulacion && userId != -1) {
                    dbHelper.simulateUnassignedCropsData(userId);
                }

                List<CropModel> datosNube = (userId != -1) ? dbHelper.getCropsByUser(userId) : dbHelper.getAllCrops();

                runOnUiThread(() -> {
                    listaCultivos = datosNube;
                    adapter.updateList(listaCultivos);
                });
            } finally {
                isUpdating = false;
            }
        }).start();
    }
}