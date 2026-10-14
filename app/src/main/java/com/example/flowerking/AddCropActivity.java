package com.example.flowerking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddCropActivity extends AppCompatActivity {

    private ImageView ivCropPhoto;
    private EditText etCropName, etBotanicalName, etLocation, etPlantingFrame;
    private Spinner spStage;
    private Button btnTakePhoto, btnSaveCrop, btnBackAdd;

    private DatabaseHelper dbHelper;
    private String savedImagePath = "";

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap bitmap = (Bitmap) extras.get("data");
                        if (bitmap != null) {
                            ivCropPhoto.setImageTintList(null);
                            ivCropPhoto.setImageBitmap(bitmap);
                            savedImagePath = guardarImagenEnDisco(bitmap);
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    abrirCamara();
                } else {
                    Toast.makeText(this, "Permiso de cámara necesario para capturar la planta", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_crop);

        dbHelper = new DatabaseHelper(this);

        ivCropPhoto = findViewById(R.id.ivCropPhoto);
        etCropName = findViewById(R.id.etCropName);
        etBotanicalName = findViewById(R.id.etBotanicalName);
        etLocation = findViewById(R.id.etLocation);
        etPlantingFrame = findViewById(R.id.etPlantingFrame);
        spStage = findViewById(R.id.spStage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSaveCrop = findViewById(R.id.btnSaveCrop);
        btnBackAdd = findViewById(R.id.btnBackAdd);

        btnBackAdd.setOnClickListener(v -> finish());

        String[] stages = {"Germinación", "Vegetativo", "Floración", "Fructificación", "Cosecha"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stages);
        spStage.setAdapter(adapter);

        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnSaveCrop.setOnClickListener(v -> guardarCultivo());
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private String guardarImagenEnDisco(Bitmap bitmap) {
        File dir = getFilesDir();
        File file = new File(dir, "planta_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void guardarCultivo() {
        String name = etCropName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "El nombre y la ubicación son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        String rawBotanical = etBotanicalName.getText().toString().trim();
        final String botanical = rawBotanical.isEmpty() ? "Variedad común" : rawBotanical;

        String rawFrame = etPlantingFrame.getText().toString().trim();
        final String frame = rawFrame.isEmpty() ? "20 x 20 cm" : rawFrame;

        final String stage = spStage.getSelectedItem() != null ? spStage.getSelectedItem().toString() : "Vegetativo";
        final int userId = SessionManager.getUserId(this);

        btnSaveCrop.setEnabled(false);
        Toast.makeText(this, "Guardando planta en tu huerto...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            long idGenerado = dbHelper.insertCrop(userId, name, botanical, location, frame, stage, savedImagePath);

            runOnUiThread(() -> {
                btnSaveCrop.setEnabled(true);
                if (idGenerado != -1) {
                    Toast.makeText(this, "¡Planta registrada con éxito!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error al guardar la planta", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}