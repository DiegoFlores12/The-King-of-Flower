package com.example.flowerking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;

public class AddCropActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;

    private ImageView ivAddCropPhoto;
    private MaterialButton btnTakePhoto;
    private EditText etCropName, etBotanicalName, etLocation, etPlantingFrame;
    private Spinner spinnerStage;
    private TextView tvScientificSource, tvScientificDetails;
    private MaterialButton btnSaveCrop;
    private Button btnAddCropBack;

    private DatabaseHelper dbHelper;
    private BotanicalService.BotanicalProfile perfilActual = null;
    private String currentPhotoPath = "";

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            ivAddCropPhoto.setImageTintList(null);
                            ivAddCropPhoto.setImageBitmap(imageBitmap);
                            btnTakePhoto.setText("📷 Cambiar Foto");
                            guardarImagenLocal(imageBitmap);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_crop);

        dbHelper = new DatabaseHelper(this);

        ivAddCropPhoto = findViewById(R.id.ivAddCropPhoto);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        etCropName = findViewById(R.id.etCropName);
        etBotanicalName = findViewById(R.id.etBotanicalName);
        etLocation = findViewById(R.id.etLocation);
        etPlantingFrame = findViewById(R.id.etPlantingFrame);
        spinnerStage = findViewById(R.id.spinnerStage);
        tvScientificSource = findViewById(R.id.tvScientificSource);
        tvScientificDetails = findViewById(R.id.tvScientificDetails);
        btnSaveCrop = findViewById(R.id.btnSaveCrop);
        btnAddCropBack = findViewById(R.id.btnAddCropBack);

        btnAddCropBack.setOnClickListener(v -> finish());

        btnTakePhoto.setOnClickListener(v -> verificarPermisoYTomarFoto());
        ivAddCropPhoto.setOnClickListener(v -> verificarPermisoYTomarFoto());

        String[] etapas = {
                "Germinación / Plántula",
                "Crecimiento Vegetativo",
                "Floración",
                "Fructificación / Engorde",
                "Maduración / Cosecha"
        };
        ArrayAdapter<String> stageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, etapas);
        spinnerStage.setAdapter(stageAdapter);
        spinnerStage.setSelection(1);

        etCropName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validarConServicioBotanico(etCropName.getText().toString().trim());
            }
        });

        etCropName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() >= 4) {
                    validarConServicioBotanico(s.toString().trim());
                }
            }
        });

        btnSaveCrop.setOnClickListener(v -> guardarCultivoEnFirebase());
    }

    private void verificarPermisoYTomarFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            abrirCamara();
        }
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo iniciar la cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara necesario para adjuntar foto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarImagenLocal(Bitmap bitmap) {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir != null) {
                File file = new File(dir, "crop_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                fos.close();
                currentPhotoPath = file.getAbsolutePath();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error guardando archivo local: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void validarConServicioBotanico(String query) {
        if (query.isEmpty()) return;

        BotanicalService.obtenerParametrosCientificos(query, perfil -> {
            perfilActual = perfil;
            if (etBotanicalName.getText().toString().trim().isEmpty() ||
                    etBotanicalName.getText().toString().equals("Variedad común") ||
                    etBotanicalName.getText().toString().equals("Solanum lycopersicum")) {
                etBotanicalName.setText(perfil.scientificName);
            }
            tvScientificSource.setText("Fuente: " + perfil.source);
            tvScientificDetails.setText("T. Óptima: " + perfil.optTempMin + "°C - " + perfil.optTempMax + "°C | Límite máx: " + perfil.maxTemp + "°C");
        });
    }

    private void guardarCultivoEnFirebase() {
        String name = etCropName.getText().toString().trim();
        String botanical = etBotanicalName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String frame = etPlantingFrame.getText().toString().trim();
        String stage = spinnerStage.getSelectedItem() != null ? spinnerStage.getSelectedItem().toString() : "Crecimiento Vegetativo";

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Completa el nombre y la ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.length() < 2) {
            Toast.makeText(this, "El nombre del cultivo debe tener al menos 2 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = SessionManager.getUserId(this);
        if (userId <= 0) {
            Toast.makeText(this, "Sesión no válida. Inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
            return;
        }

        if (botanical.isEmpty()) {
            botanical = "Variedad común";
        }
        if (frame.isEmpty()) {
            frame = "20 x 20 cm";
        }

        if (perfilActual == null) {
            perfilActual = new BotanicalService.BotanicalProfile(
                    name, botanical, 10.0, 18.0, 27.0, 35.0, 40.0, 80.0, "Cada 2 días (500 ml)", "FAO EcoCrop"
            );
        }

        btnSaveCrop.setEnabled(false);
        Toast.makeText(this, "Guardando en Firebase...", Toast.LENGTH_SHORT).show();

        BotanicalService.BotanicalProfile p = perfilActual;
        String finalBotanical = botanical;
        String finalFrame = frame;
        String photoParaGuardar = currentPhotoPath;

        new Thread(() -> {
            long nuevoId = dbHelper.insertCrop(
                    userId, name, finalBotanical, location, finalFrame, stage, photoParaGuardar,
                    p.minTemp, p.optTempMin, p.optTempMax, p.maxTemp, p.minMoisture, p.maxMoisture, p.source, p.wateringFreq
            );

            runOnUiThread(() -> {
                btnSaveCrop.setEnabled(true);
                if (nuevoId > 0) {
                    Toast.makeText(this, "¡Cultivo registrado en Firebase!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (nuevoId == -2) {
                    Toast.makeText(this, "Ya existe un cultivo con ese nombre en esa ubicación", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Error de conexión al guardar en Firebase", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}