package com.example.flowerking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class IrrigationActivity extends AppCompatActivity {

    private Spinner spinnerCrops;
    private TextView tvTargetCropForced;
    private MaterialButton btnForceWatering;
    private LinearLayout btnSelectDate, btnSelectTime;
    private TextView tvSelectedDate, tvSelectedTime;
    private Spinner spinnerFrequency;
    private MaterialButton btnSaveSchedule;
    private BottomNavigationView bottomNav;

    private DatabaseHelper dbHelper;
    private List<CropModel> listaCultivos = new ArrayList<>();
    private CropModel cultivoSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irrigation);

        dbHelper = new DatabaseHelper(this);

        spinnerCrops = findViewById(R.id.spinnerCrops);
        tvTargetCropForced = findViewById(R.id.tvTargetCropForced);
        btnForceWatering = findViewById(R.id.btnForceWatering);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        bottomNav = findViewById(R.id.bottomNavigationIrrigation);

        String[] frecuencias = {
                "Diario (1 vez al día)",
                "Cada 2 días (Recomendado)",
                "Cada 3 días",
                "Semanal (1 vez por semana)",
                "Personalizado"
        };
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, frecuencias);
        spinnerFrequency.setAdapter(freqAdapter);

        btnSelectDate.setOnClickListener(v -> abrirSelectorFecha());
        btnSelectTime.setOnClickListener(v -> abrirSelectorHora());

        spinnerCrops.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!listaCultivos.isEmpty() && position < listaCultivos.size()) {
                    cultivoSeleccionado = listaCultivos.get(position);
                    tvTargetCropForced.setText("Riego Forzado: " + cultivoSeleccionado.getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnForceWatering.setOnClickListener(v -> {
            if (cultivoSeleccionado == null) {
                Toast.makeText(this, "No hay ningún cultivo seleccionado", Toast.LENGTH_SHORT).show();
                return;
            }

            btnForceWatering.setEnabled(false);
            Toast.makeText(this, "Activando bomba para " + cultivoSeleccionado.getName() + "...", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                boolean exito = dbHelper.waterCrop(cultivoSeleccionado.getId());
                runOnUiThread(() -> {
                    btnForceWatering.setEnabled(true);
                    if (exito) {
                        Toast.makeText(this, "¡Riego ejecutado con éxito!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al registrar el riego", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        btnSaveSchedule.setOnClickListener(v -> {
            if (cultivoSeleccionado == null) {
                Toast.makeText(this, "Selecciona una planta primero", Toast.LENGTH_SHORT).show();
                return;
            }

            String fechaHora = tvSelectedDate.getText().toString() + " " + tvSelectedTime.getText().toString();
            String frecuencia = spinnerFrequency.getSelectedItem().toString();

            btnSaveSchedule.setEnabled(false);
            Toast.makeText(this, "Guardando cronograma...", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                boolean exito = dbHelper.updateIrrigationSchedule(cultivoSeleccionado.getId(), fechaHora, frecuencia);
                runOnUiThread(() -> {
                    btnSaveSchedule.setEnabled(true);
                    if (exito) {
                        Toast.makeText(this, "¡Cronograma actualizado con éxito!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al guardar cronograma", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        bottomNav.setSelectedItemId(R.id.nav_irrigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_plants) {
                Intent intent = new Intent(IrrigationActivity.this, CropsActivity.class);
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
        cargarCultivosLocales();
    }

    private void cargarCultivosLocales() {
        int userId = SessionManager.getUserId(this);

        new Thread(() -> {
            List<CropModel> cultivos = (userId != -1) ? dbHelper.getCropsByUser(userId) : dbHelper.getAllCrops();

            runOnUiThread(() -> {
                listaCultivos = cultivos;
                if (!listaCultivos.isEmpty()) {
                    List<String> titulos = new ArrayList<>();
                    for (CropModel c : listaCultivos) {
                        titulos.add(c.getName() + " (" + c.getLocation() + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            titulos
                    );
                    spinnerCrops.setAdapter(adapter);
                    cultivoSeleccionado = listaCultivos.get(0);
                    tvTargetCropForced.setText("Riego Forzado: " + cultivoSeleccionado.getName());
                } else {
                    tvTargetCropForced.setText("Sin cultivos en la huerta");
                }
            });
        }).start();
    }

    private void abrirSelectorFecha() {
        Calendar c = Calendar.getInstance();
        int anio = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            tvSelectedDate.setText(fecha);
        }, anio, mes, dia);
        dialog.show();
    }

    private void abrirSelectorHora() {
        Calendar c = Calendar.getInstance();
        int hora = c.get(Calendar.HOUR_OF_DAY);
        int minuto = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String horaFormateada = String.format(Locale.getDefault(), "%02d:%02d hrs", hourOfDay, minute);
            tvSelectedTime.setText(horaFormateada);
        }, hora, minuto, true);
        dialog.show();
    }
}