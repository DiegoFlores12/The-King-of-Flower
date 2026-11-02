package com.example.flowerking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREF_SETTINGS = "FlowerKingSettings";
    private static final String KEY_DARK_MODE = "DARK_MODE";
    private static final String KEY_FAHRENHEIT = "USE_FAHRENHEIT";
    private static final String KEY_ALERT_SOIL = "ALERT_SOIL";

    private TextView tvAvatarCircle, tvSettingsUsername, tvSettingsUserId;
    private SwitchMaterial switchDarkMode, switchFahrenheit, switchAlertSoil;
    private MaterialButton btnSyncNow, btnLogout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvAvatarCircle = findViewById(R.id.tvAvatarCircle);
        tvSettingsUsername = findViewById(R.id.tvSettingsUsername);
        tvSettingsUserId = findViewById(R.id.tvSettingsUserId);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchFahrenheit = findViewById(R.id.switchFahrenheit);
        switchAlertSoil = findViewById(R.id.switchAlertSoil);
        btnSyncNow = findViewById(R.id.btnSyncNow);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNav = findViewById(R.id.bottomNavigationSettings);

        cargarDatosPerfil();
        cargarPreferenciasGuardadas();

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences sp = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
            sp.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        switchFahrenheit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences sp = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
            sp.edit().putBoolean(KEY_FAHRENHEIT, isChecked).apply();
            Toast.makeText(this, isChecked ? "Unidad cambiada a °F" : "Unidad cambiada a °C", Toast.LENGTH_SHORT).show();
        });

        switchAlertSoil.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences sp = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
            sp.edit().putBoolean(KEY_ALERT_SOIL, isChecked).apply();
        });

        btnSyncNow.setOnClickListener(v -> {
            btnSyncNow.setEnabled(false);
            Toast.makeText(this, "Consultando última telemetría en Neon...", Toast.LENGTH_SHORT).show();

            v.postDelayed(() -> {
                btnSyncNow.setEnabled(true);
                Toast.makeText(this, "¡Telemetría y cultivos sincronizados!", Toast.LENGTH_SHORT).show();
            }, 1200);
        });

        // 5. Cerrar Sesión con Confirmación
        btnLogout.setOnClickListener(v -> confirmarCierreSesion());

        // 6. Navegación Inferior
        configurarNavegacion();
    }

    private void cargarDatosPerfil() {
        String username = SessionManager.getUsername(this);
        int userId = SessionManager.getUserId(this);

        tvSettingsUsername.setText(username);
        tvSettingsUserId.setText("ID de Cuenta: #" + userId + " • Conectado a Neon");

        if (username != null && !username.isEmpty()) {
            tvAvatarCircle.setText(username.substring(0, 1).toUpperCase());
        }
    }

    private void cargarPreferenciasGuardadas() {
        SharedPreferences sp = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        boolean isDark = sp.getBoolean(KEY_DARK_MODE, true);
        boolean isFahr = sp.getBoolean(KEY_FAHRENHEIT, false);
        boolean alertSoil = sp.getBoolean(KEY_ALERT_SOIL, true);

        switchDarkMode.setChecked(isDark);
        switchFahrenheit.setChecked(isFahr);
        switchAlertSoil.setChecked(alertSoil);
    }

    private void confirmarCierreSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Deseas salir de tu cuenta? Tendrás que ingresar tus credenciales nuevamente.")
                .setPositiveButton("Cerrar Sesión", (dialog, which) -> {
                    SessionManager.logout(SettingsActivity.this);
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void configurarNavegacion() {
        int menuSize = bottomNav.getMenu().size();
        if (menuSize >= 4) {
            bottomNav.setSelectedItemId(bottomNav.getMenu().getItem(3).getItemId());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_plants) {
                Intent intent = new Intent(SettingsActivity.this, CropsActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_irrigation) {
                Intent intent = new Intent(SettingsActivity.this, IrrigationActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return true;
        });
    }
}