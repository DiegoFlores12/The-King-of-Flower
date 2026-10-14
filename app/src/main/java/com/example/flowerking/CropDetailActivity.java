package com.example.flowerking;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.io.File;

public class CropDetailActivity extends AppCompatActivity {

    private ImageView ivCropDetailPhoto;
    private TextView tvDetailName, tvDetailBotanical, tvDetailStage, tvDetailStatus;
    private TextView tvDetailLocation, tvDetailFrame, tvDetailDensity;
    private TextView tvDetailTemp, tvDetailMoisture, tvDetailWateringFreq, tvDetailNextWatering;
    private View btnChangeStage;
    private Button btnBack;
    private MaterialButton btnWaterNow, btnDeleteCrop;

    private DatabaseHelper dbHelper;
    private CropModel crop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_detail);

        dbHelper = new DatabaseHelper(this);

        ivCropDetailPhoto = findViewById(R.id.ivCropDetailPhoto);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailBotanical = findViewById(R.id.tvDetailBotanical);
        tvDetailStage = findViewById(R.id.tvDetailStage);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailFrame = findViewById(R.id.tvDetailFrame);
        tvDetailDensity = findViewById(R.id.tvDetailDensity);
        tvDetailTemp = findViewById(R.id.tvDetailTemp);
        tvDetailMoisture = findViewById(R.id.tvDetailMoisture);
        tvDetailWateringFreq = findViewById(R.id.tvDetailWateringFreq);
        tvDetailNextWatering = findViewById(R.id.tvDetailNextWatering);

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
        } else {
            Toast.makeText(this, "Error al cargar los datos del cultivo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cargarDatosEnVista() {
        tvDetailName.setText(crop.getName());
        tvDetailBotanical.setText(crop.getBotanicalName());
        tvDetailStage.setText("Etapa: " + crop.getStage());
        tvDetailStatus.setText(crop.getStatus());
        tvDetailLocation.setText("Ubicación: " + crop.getLocation());
        tvDetailFrame.setText("Marco de siembra: " + crop.getPlantingFrame());
        tvDetailDensity.setText("Densidad: " + crop.getDensity());
        tvDetailTemp.setText(crop.getSubstrateTemp());
        tvDetailMoisture.setText(crop.getMoisture());
        tvDetailWateringFreq.setText("Frecuencia: " + crop.getWateringFreq());
        tvDetailNextWatering.setText("Próximo riego: " + crop.getNextWatering());

        if (crop.getPhotoPath() != null && !crop.getPhotoPath().isEmpty()) {
            File imgFile = new File(crop.getPhotoPath());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ivCropDetailPhoto.setImageTintList(null);
                ivCropDetailPhoto.setImageBitmap(myBitmap);
            }
        }
    }

    private void mostrarAsistenteEtapas() {
        final String[] etapas = {
                "Germinación / Plántula",
                "Crecimiento Vegetativo",
                "Floración",
                "Fructificación / Engorde",
                "Maduración / Cosecha"
        };

        final String[] descripciones = {
                "🌱 Plántula: Primeros brotes y hojas iniciales. Tallo tierno y frágil.",
                "🌿 Vegetativo: Hojas en expansión y tallo firme. Aún sin flores visibles.",
                "🌸 Floración: Aparición y apertura de capullos o flores.",
                "🍅 Fructificación: Flores marchitándose y formación de frutos verdes.",
                "🧺 Cosecha: Frutos desarrollados con color y tamaño final listos para cosechar."
        };

        CharSequence[] opcionesConGuia = new CharSequence[etapas.length];
        for (int i = 0; i < etapas.length; i++) {
            opcionesConGuia[i] = etapas[i] + "\n" + descripciones[i];
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Cómo se ve tu cultivo?");
        builder.setItems(opcionesConGuia, (dialog, which) -> {
            String etapaElegida = etapas[which];
            actualizarEtapa(etapaElegida);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void actualizarEtapa(String nuevaEtapa) {
        new Thread(() -> {
            boolean exito = dbHelper.updateCropStage(crop.getId(), nuevaEtapa);

            runOnUiThread(() -> {
                if (exito) {
                    crop.setStage(nuevaEtapa);
                    tvDetailStage.setText("Etapa: " + nuevaEtapa);
                    Toast.makeText(this, "Etapa actualizada con éxito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error al actualizar la etapa", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
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
                    tvDetailStatus.setText("Óptimo");
                    tvDetailNextWatering.setText("Próximo riego: Mañana 18:00");
                    Toast.makeText(this, "¡Cultivo regado con éxito!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error al registrar el riego", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar cultivo?")
                .setMessage("¿Estás seguro de que deseas eliminar permanentemente \"" + crop.getName() + "\" de tu huerta?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarCultivo())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCultivo() {
        btnDeleteCrop.setEnabled(false);
        Toast.makeText(this, "Eliminando cultivo...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            boolean exito = dbHelper.deleteCrop(crop.getId());

            runOnUiThread(() -> {
                if (exito) {
                    Toast.makeText(this, "Cultivo eliminado del huerto", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnDeleteCrop.setEnabled(true);
                    Toast.makeText(this, "Error al eliminar el cultivo", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}