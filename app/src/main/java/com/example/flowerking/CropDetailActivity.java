package com.example.flowerking;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;


public class CropDetailActivity extends AppCompatActivity {

    private static final String DEVICE_ID = "ESP32_PRINCIPAL";

    private ImageView ivCropDetailPhoto;
    private TextView tvDetailName, tvDetailBotanical, tvDetailStage, tvDetailStatus, tvDetailBotanicalSource;
    private TextView tvDetailLocation, tvDetailFrame, tvDetailDensity;
    private TextView tvDetailTemp, tvDetailMoisture, tvDetailWateringFreq, tvDetailNextWatering;
    private Button btnBack;
    private MaterialButton btnChangeStage, btnWaterNow, btnDeleteCrop;

    private TextView tvIotStatusBadge, tvIotDescription;
    private MaterialButton btnLinkDevice;
    private RecyclerView rvCropHistory;
    private TextView tvHistoryEmpty;

    private CropHistoryAdapter historyAdapter;

    private List<CropHistoryItem> historyList =
            new ArrayList<>();

    private DatabaseHelper dbHelper;
    private CropModel crop;
    private DatabaseHelper.IotAssignmentInfo currentIotInfo = new DatabaseHelper.IotAssignmentInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_detail);

        dbHelper = new DatabaseHelper(this);

        rvCropHistory = findViewById(R.id.rvCropHistory);
        tvHistoryEmpty = findViewById(R.id.tvHistoryEmpty);

        rvCropHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        historyAdapter = new CropHistoryAdapter(historyList);

        rvCropHistory.setAdapter(historyAdapter);

        ivCropDetailPhoto = findViewById(R.id.ivCropDetailPhoto);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailBotanical = findViewById(R.id.tvDetailBotanical);
        tvDetailStage = findViewById(R.id.tvDetailStage);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailBotanicalSource = findViewById(R.id.tvDetailBotanicalSource);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailFrame = findViewById(R.id.tvDetailFrame);
        tvDetailDensity = findViewById(R.id.tvDetailDensity);
        tvDetailTemp = findViewById(R.id.tvDetailTemp);
        tvDetailMoisture = findViewById(R.id.tvDetailMoisture);
        tvDetailWateringFreq = findViewById(R.id.tvDetailWateringFreq);
        tvDetailNextWatering = findViewById(R.id.tvDetailNextWatering);

        tvIotStatusBadge = findViewById(R.id.tvIotStatusBadge);
        tvIotDescription = findViewById(R.id.tvIotDescription);
        btnLinkDevice = findViewById(R.id.btnLinkDevice);

        btnBack = findViewById(R.id.btnBack);
        btnChangeStage = findViewById(R.id.btnChangeStage);
        btnWaterNow = findViewById(R.id.btnWaterNow);
        btnDeleteCrop = findViewById(R.id.btnDeleteCrop);

        btnBack.setOnClickListener(v -> finish());

        crop = (CropModel) getIntent().getSerializableExtra("EXTRA_CROP");
        if (crop != null) {
            cargarDatosEnVista();
            btnChangeStage.setOnClickListener(v -> mostrarAsistenteEtapas());
            btnWaterNow.setOnClickListener(v -> ejecutarRiego());
            btnDeleteCrop.setOnClickListener(v -> confirmarEliminacion());
            btnLinkDevice.setOnClickListener(v -> gestionarClickIot());
        } else {
            Toast.makeText(this, "Error al cargar los datos del cultivo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (crop != null) {
            consultarAsignacionIot();
        }
    }

    private void cargarDatosEnVista() {
        tvDetailName.setText(crop.getName());
        tvDetailBotanical.setText(crop.getBotanicalName());
        tvDetailStage.setText("Etapa: " + crop.getStage());
        tvDetailLocation.setText("Ubicación: " + crop.getLocation());
        tvDetailFrame.setText("Marco de siembra: " + crop.getPlantingFrame());
        tvDetailDensity.setText("Densidad: " + crop.getDensity());
        tvDetailTemp.setText(crop.getSubstrateTemp());
        tvDetailMoisture.setText(crop.getMoisture());
        tvDetailWateringFreq.setText("Frecuencia: " + crop.getWateringFreq());
        tvDetailNextWatering.setText("Próximo riego: " + crop.getNextWatering());

        tvDetailBotanicalSource.setText(String.format(Locale.US,
                "Límites Oficiales (%s):\n• Rango Óptimo: %.0f°C a %.0f°C\n• Umbrales Límite: Mín %.0f°C | Máx %.0f°C\n• Humedad recomendada: %.0f%% a %.0f%%",
                crop.getBotanicalSource(), crop.getOptTempMin(), crop.getOptTempMax(), crop.getMinTemp(), crop.getMaxTemp(), crop.getMinMoisture(), crop.getMaxMoisture()));

        evaluarEstadoFitosanitario();

        if (crop.getPhotoPath() != null && !crop.getPhotoPath().isEmpty()) {
            File imgFile = new File(crop.getPhotoPath());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ivCropDetailPhoto.setImageTintList(null);
                ivCropDetailPhoto.setImageBitmap(myBitmap);
            }
        }

        cargarHistorial();
    }

    private void evaluarEstadoFitosanitario() {
        double tempActual = extraerNumero(crop.getSubstrateTemp());
        double humActual = extraerNumero(crop.getMoisture());

        StringBuilder diag = new StringBuilder();
        int colorEstado = ContextCompat.getColor(this, R.color.fk_accent_green);

        if (tempActual > crop.getMaxTemp()) {
            diag.append("🔴 ALERTA: Estrés térmico (").append(tempActual).append("°C > máx ").append(crop.getMaxTemp()).append("°C). ");
            colorEstado = 0xFFFF4D4D;
        } else if (tempActual < crop.getMinTemp()) {
            diag.append("🔵 ALERTA: Frío crítico (").append(tempActual).append("°C < mín ").append(crop.getMinTemp()).append("°C). ");
            colorEstado = 0xFF52B2BF;
        } else if (tempActual >= crop.getOptTempMin() && tempActual <= crop.getOptTempMax()) {
            diag.append("🟢 Temperatura Óptima (").append(tempActual).append("°C). ");
        } else {
            diag.append("🟡 Temperatura Aceptable (").append(tempActual).append("°C). ");
        }

        if (humActual < crop.getMinMoisture()) {
            diag.append("⚠️ Sustrato seco (").append(humActual).append("% < ").append(crop.getMinMoisture()).append("%).");
            if (colorEstado == ContextCompat.getColor(this, R.color.fk_accent_green)) {
                colorEstado = 0xFFFFB84D;
            }
        } else if (humActual > crop.getMaxMoisture()) {
            diag.append("⚠️ Exceso de agua (").append(humActual).append("% > ").append(crop.getMaxMoisture()).append("%).");
            if (colorEstado == ContextCompat.getColor(this, R.color.fk_accent_green)) {
                colorEstado = 0xFFFFB84D;
            }
        } else {
            diag.append("Humedad ideal.");
        }

        tvDetailStatus.setText(diag.toString());
        tvDetailStatus.setTextColor(colorEstado);
    }

    private double extraerNumero(String texto) {
        try {
            if (texto == null) return 0.0;
            String limpio = texto.replaceAll("[^0-9.]", "");
            return Double.parseDouble(limpio);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void consultarAsignacionIot() {
        btnLinkDevice.setEnabled(false);

        new Thread(() -> {
            DatabaseHelper.IotAssignmentInfo info = dbHelper.checkDeviceAssignment(DEVICE_ID, crop.getId());

            runOnUiThread(() -> {
                btnLinkDevice.setEnabled(true);
                currentIotInfo = info;

                if (info.isAssignedToThisCrop) {
                    tvIotStatusBadge.setText("● Sensor Activo en Vivo");
                    tvIotStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.fk_accent_green));
                    tvIotDescription.setText("El nodo ESP32 está transmitiendo telemetría directa a esta planta.");
                    btnLinkDevice.setText("Desvincular sensor de esta planta");
                    btnLinkDevice.setBackgroundColor(0xFF3B1E1E);
                } else if (info.assignedCropId != -1) {
                    tvIotStatusBadge.setText("○ Modo Manual");
                    tvIotStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.fk_text_secondary));
                    tvIotDescription.setText("El ESP32 está actualmente asignado a: \"" + info.assignedCropName + "\".");
                    btnLinkDevice.setText("Transferir ESP32 a esta planta");
                    btnLinkDevice.setBackgroundColor(ContextCompat.getColor(this, R.color.fk_card_stroke));
                } else {
                    tvIotStatusBadge.setText("○ Modo Manual");
                    tvIotStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.fk_text_secondary));
                    tvIotDescription.setText("El sensor ESP32 está disponible sin planta asignada.");
                    btnLinkDevice.setText("Conectar ESP32 a esta planta");
                    btnLinkDevice.setBackgroundColor(ContextCompat.getColor(this, R.color.fk_card_stroke));
                }
            });
        }).start();
    }

    private void gestionarClickIot() {
        if (currentIotInfo.isAssignedToThisCrop) {
            new AlertDialog.Builder(this)
                    .setTitle("Desvincular Hardware")
                    .setMessage("¿Deseas desvincular el ESP32 de " + crop.getName() + "? El cultivo pasará a modo manual.")
                    .setPositiveButton("Desvincular", (dialog, which) -> ejecutarDesvinculacion())
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else if (currentIotInfo.assignedCropId != -1) {
            new AlertDialog.Builder(this)
                    .setTitle("Reasignar Sensor IoT")
                    .setMessage("El ESP32 está conectado actualmente a \"" + currentIotInfo.assignedCropName + "\".\n\n¿Deseas transferir el sensor a \"" + crop.getName() + "\"?")
                    .setPositiveButton("Sí, transferir aquí", (dialog, which) -> ejecutarAsignacion())
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            ejecutarAsignacion();
        }
    }

    private void ejecutarAsignacion() {
        btnLinkDevice.setEnabled(false);
        Toast.makeText(this, "Vinculando ESP32 en Neon...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            boolean exito = dbHelper.assignDeviceToCrop(DEVICE_ID, crop.getId());

            runOnUiThread(() -> {
                btnLinkDevice.setEnabled(true);
                if (exito) {
                    Toast.makeText(this, "¡ESP32 asignado a " + crop.getName() + "!", Toast.LENGTH_SHORT).show();
                    consultarAsignacionIot();
                } else {
                    Toast.makeText(this, "Error al vincular con Neon", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void ejecutarDesvinculacion() {
        btnLinkDevice.setEnabled(false);
        Toast.makeText(this, "Desvinculando...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            boolean exito = dbHelper.unassignDevice(DEVICE_ID);

            runOnUiThread(() -> {
                btnLinkDevice.setEnabled(true);
                if (exito) {
                    Toast.makeText(this, "ESP32 desvinculado con éxito", Toast.LENGTH_SHORT).show();
                    consultarAsignacionIot();
                } else {
                    Toast.makeText(this, "Error al desvincular en Neon", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void mostrarAsistenteEtapas() {
        final String[] etapas = {
                "Germinación / Plántula",
                "Crecimiento Vegetativo",
                "Floración",
                "Fructificación / Engorde",
                "Maduración / Cosecha"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Actualizar Etapa");
        builder.setItems(etapas, (dialog, which) -> {
            String etapaElegida = etapas[which];
            new Thread(() -> {
                boolean exito = dbHelper.updateCropStage(crop.getId(), etapaElegida);
                runOnUiThread(() -> {
                    if (exito) {
                        crop.setStage(etapaElegida);
                        cargarHistorial();
                        tvDetailStage.setText("Etapa: " + etapaElegida);
                        Toast.makeText(this, "Etapa actualizada", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void ejecutarRiego() {
        btnWaterNow.setEnabled(false);
        new Thread(() -> {
            boolean exito = dbHelper.waterCrop(crop.getId());
            runOnUiThread(() -> {
                btnWaterNow.setEnabled(true);
                if (exito) {
                    crop.setMoisture("78%");
                    crop.setStatus("Óptimo");
                    tvDetailMoisture.setText("78%");
                    evaluarEstadoFitosanitario();
                    tvDetailNextWatering.setText("Próximo riego: Mañana 18:00");
                    Toast.makeText(this, "¡Cultivo regado con éxito!", Toast.LENGTH_SHORT).show();
                    cargarHistorial();
                }
            });
        }).start();
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar cultivo?")
                .setMessage("¿Estás seguro de que deseas eliminar permanentemente \"" + crop.getName() + "\" de tu huerta?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    new Thread(() -> {
                        boolean exito = dbHelper.deleteCrop(crop.getId());
                        runOnUiThread(() -> {
                            if (exito) {
                                Toast.makeText(this, "Cultivo eliminado", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cargarHistorial() {

        tvHistoryEmpty.setText("Cargando actividades...");
        tvHistoryEmpty.setVisibility(View.VISIBLE);

        new Thread(() -> {

            List<CropHistoryItem> datos =
                    dbHelper.getCropHistory(crop.getId());

            runOnUiThread(() -> {

                historyList.clear();
                historyList.addAll(datos);

                historyAdapter.notifyDataSetChanged();

                if (historyList.isEmpty()) {

                    tvHistoryEmpty.setText(
                            "Todavía no hay actividades registradas."
                    );

                    tvHistoryEmpty.setVisibility(View.VISIBLE);

                } else {

                    tvHistoryEmpty.setVisibility(View.GONE);
                }
            });

        }).start();
    }
}