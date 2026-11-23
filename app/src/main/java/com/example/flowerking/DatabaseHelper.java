package com.example.flowerking;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DatabaseHelper {

    private static final String TAG = "FirebaseDB";
    private final FirebaseFirestore db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DatabaseHelper(Context context) {
        this.db = FirebaseFirestore.getInstance();
    }

    private <T> T awaitTask(Task<T> task) {
        try {
            return executor.submit(() -> Tasks.await(task, 8, TimeUnit.SECONDS)).get(9, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Error de conexión o consulta en Firestore: " + e.getMessage());
            return null;
        }
    }

    private boolean executeVoidTask(Task<Void> task) {
        try {
            executor.submit(() -> {
                Tasks.await(task, 8, TimeUnit.SECONDS);
                return true;
            }).get(9, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al escribir en Firestore: " + e.getMessage());
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password;
        }
    }

    public int registerUser(String username, String email, String password) {
        QuerySnapshot existing = awaitTask(
                db.collection("users").whereEqualTo("username", username).limit(1).get()
        );
        if (existing == null) {
            return -2;
        }
        if (!existing.isEmpty()) {
            return -1;
        }

        int newId = (int) (System.currentTimeMillis() % 100000000L);
        if (newId <= 0) newId = 1;

        Map<String, Object> user = new HashMap<>();
        user.put("id", newId);
        user.put("username", username);
        user.put("email", email);
        user.put("password", hashPassword(password));

        boolean saved = executeVoidTask(
                db.collection("users").document(String.valueOf(newId)).set(user)
        );
        return saved ? newId : -2;
    }

    public int checkLogin(String username, String password) {
        QuerySnapshot snapshot = awaitTask(
                db.collection("users")
                        .whereEqualTo("username", username)
                        .whereEqualTo("password", hashPassword(password))
                        .limit(1)
                        .get()
        );
        if (snapshot == null) {
            return -2;
        }
        if (!snapshot.isEmpty()) {
            Long id = snapshot.getDocuments().get(0).getLong("id");
            return id != null ? id.intValue() : -1;
        }
        return -1;
    }

    public List<CropModel> getCropsByUser(int userId) {
        QuerySnapshot snapshot = awaitTask(
                db.collection("crops").whereEqualTo("user_id", userId).get()
        );
        return parsearCultivos(snapshot);
    }

    public List<CropModel> getAllCrops() {
        QuerySnapshot snapshot = awaitTask(
                db.collection("crops").get()
        );
        return parsearCultivos(snapshot);
    }

    private List<CropModel> parsearCultivos(QuerySnapshot snapshot) {
        List<CropModel> list = new ArrayList<>();
        if (snapshot == null) return list;

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Long idLong = doc.getLong("id");
            int id = idLong != null ? idLong.intValue() : 0;
            String name = doc.getString("name") != null ? doc.getString("name") : "Sin nombre";
            String botanical = doc.getString("botanical_name") != null ? doc.getString("botanical_name") : "Variedad común";
            String location = doc.getString("location") != null ? doc.getString("location") : "Sin ubicación";
            String frame = doc.getString("planting_frame") != null ? doc.getString("planting_frame") : "20 x 20 cm";
            String density = doc.getString("density") != null ? doc.getString("density") : "12 plantas/m²";
            String stage = doc.getString("stage") != null ? doc.getString("stage") : "Vegetativo";
            String photoPath = doc.getString("photo_path") != null ? doc.getString("photo_path") : "";
            double temp = doc.getDouble("substrate_temp") != null ? doc.getDouble("substrate_temp") : 24.0;
            double moisture = doc.getDouble("moisture") != null ? doc.getDouble("moisture") : 60.0;
            String status = doc.getString("status") != null ? doc.getString("status") : "Óptimo";
            String nextW = doc.getString("next_watering") != null ? doc.getString("next_watering") : "Hoy 18:00";
            String freqW = doc.getString("watering_freq") != null ? doc.getString("watering_freq") : "Cada 2 días";

            CropModel crop = new CropModel(
                    id, name, botanical, stage, location, frame, density, status,
                    String.format(Locale.US, "%.1f°C", temp),
                    String.format(Locale.US, "%.0f%%", moisture),
                    nextW, freqW, photoPath
            );

            crop.setMinTemp(doc.getDouble("min_temp") != null ? doc.getDouble("min_temp") : 10.0);
            crop.setOptTempMin(doc.getDouble("opt_temp_min") != null ? doc.getDouble("opt_temp_min") : 18.0);
            crop.setOptTempMax(doc.getDouble("opt_temp_max") != null ? doc.getDouble("opt_temp_max") : 27.0);
            crop.setMaxTemp(doc.getDouble("max_temp") != null ? doc.getDouble("max_temp") : 35.0);
            crop.setMinMoisture(doc.getDouble("min_moisture") != null ? doc.getDouble("min_moisture") : 40.0);
            crop.setMaxMoisture(doc.getDouble("max_moisture") != null ? doc.getDouble("max_moisture") : 80.0);
            crop.setBotanicalSource(doc.getString("botanical_source") != null ? doc.getString("botanical_source") : "FAO EcoCrop Database");

            list.add(crop);
        }

        Collections.sort(list, (a, b) -> Integer.compare(b.getId(), a.getId()));
        return list;
    }

    public long insertCrop(int userId, String name, String botanical, String location, String frame, String stage, String photoPath,
                           double minT, double optMinT, double optMaxT, double maxT, double minM, double maxM, String source, String wateringFreq) {
        QuerySnapshot duplicate = awaitTask(
                db.collection("crops")
                        .whereEqualTo("user_id", userId)
                        .whereEqualTo("name", name)
                        .whereEqualTo("location", location)
                        .limit(1)
                        .get()
        );
        if (duplicate == null) {
            return -1;
        }
        if (!duplicate.isEmpty()) {
            return -2;
        }

        long newId = System.currentTimeMillis() % 100000000L;
        if (newId <= 0) newId = 1;

        Map<String, Object> crop = new HashMap<>();
        crop.put("id", newId);
        crop.put("user_id", userId);
        crop.put("name", name);
        crop.put("botanical_name", botanical);
        crop.put("location", location);
        crop.put("planting_frame", frame);
        crop.put("density", "12 plantas/m²");
        crop.put("stage", stage);
        crop.put("photo_path", photoPath);
        crop.put("substrate_temp", 24.0);
        crop.put("moisture", 65.0);
        crop.put("water_level", 0.0);
        crop.put("status", "Óptimo");
        crop.put("next_watering", "Hoy 20:00");
        crop.put("watering_freq", wateringFreq);
        crop.put("min_temp", minT);
        crop.put("opt_temp_min", optMinT);
        crop.put("opt_temp_max", optMaxT);
        crop.put("max_temp", maxT);
        crop.put("min_moisture", minM);
        crop.put("max_moisture", maxM);
        crop.put("botanical_source", source);

        boolean saved = executeVoidTask(
                db.collection("crops").document(String.valueOf(newId)).set(crop)
        );
        if (saved) {
            registerCropHistory((int) newId, "CREACIÓN", "Cultivo registrado en Firebase.");
            return newId;
        }
        return -1;
    }

    public boolean updateCropStage(int id, String newStage) {
        boolean updated = executeVoidTask(
                db.collection("crops").document(String.valueOf(id)).update("stage", newStage)
        );
        if (updated) {
            registerCropHistory(id, "ETAPA", "El cultivo cambió a la etapa: " + newStage);
        }
        return updated;
    }

    public boolean waterCrop(int id) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("moisture", 78.0);
        updates.put("status", "Óptimo");
        updates.put("next_watering", "Mañana 18:00");

        boolean updated = executeVoidTask(
                db.collection("crops").document(String.valueOf(id)).update(updates)
        );
        if (updated) {
            registerCropHistory(id, "RIEGO", "Se realizó un riego manual correctamente.");
        }
        return updated;
    }

    public boolean updateIrrigationSchedule(int id, String nextWatering, String wateringFreq) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("next_watering", nextWatering);
        updates.put("watering_freq", wateringFreq);

        return executeVoidTask(
                db.collection("crops").document(String.valueOf(id)).update(updates)
        );
    }

    public boolean deleteCrop(int id) {
        return executeVoidTask(
                db.collection("crops").document(String.valueOf(id)).delete()
        );
    }

    public static class IotAssignmentInfo {
        public int assignedCropId = -1;
        public String assignedCropName = "";
        public boolean isAssignedToThisCrop = false;
    }

    public IotAssignmentInfo checkDeviceAssignment(String deviceId, int currentCropId) {
        IotAssignmentInfo info = new IotAssignmentInfo();
        DocumentSnapshot doc = awaitTask(db.collection("iot_devices").document(deviceId).get());

        if (doc != null && doc.exists() && doc.getLong("assigned_crop_id") != null) {
            int assignedId = doc.getLong("assigned_crop_id").intValue();
            info.assignedCropId = assignedId;
            info.isAssignedToThisCrop = (assignedId == currentCropId);

            DocumentSnapshot cropDoc = awaitTask(db.collection("crops").document(String.valueOf(assignedId)).get());
            if (cropDoc != null && cropDoc.exists() && cropDoc.getString("name") != null) {
                info.assignedCropName = cropDoc.getString("name");
            } else {
                info.assignedCropName = "Otro cultivo";
            }
        }
        return info;
    }

    public boolean assignDeviceToCrop(String deviceId, int cropId) {
        Map<String, Object> data = new HashMap<>();
        data.put("device_id", deviceId);
        data.put("assigned_crop_id", cropId);

        return executeVoidTask(
                db.collection("iot_devices").document(deviceId).set(data)
        );
    }

    public boolean unassignDevice(String deviceId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("assigned_crop_id", null);

        return executeVoidTask(
                db.collection("iot_devices").document(deviceId).update(updates)
        );
    }

    public boolean registerCropHistory(int cropId, String eventType, String description) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fecha = sdf.format(new Date());

        Map<String, Object> item = new HashMap<>();
        item.put("crop_id", cropId);
        item.put("event_type", eventType);
        item.put("description", description);
        item.put("fecha", fecha);
        item.put("timestamp", System.currentTimeMillis());

        return executeVoidTask(
                db.collection("crop_history").add(item).continueWith(task -> null)
        );
    }

    public List<CropHistoryItem> getCropHistory(int cropId) {
        List<CropHistoryItem> historial = new ArrayList<>();
        QuerySnapshot snapshot = awaitTask(
                db.collection("crop_history").whereEqualTo("crop_id", cropId).get()
        );

        if (snapshot == null) return historial;

        List<DocumentSnapshot> docs = new ArrayList<>(snapshot.getDocuments());
        Collections.sort(docs, (a, b) -> {
            Long tA = a.getLong("timestamp") != null ? a.getLong("timestamp") : 0L;
            Long tB = b.getLong("timestamp") != null ? b.getLong("timestamp") : 0L;
            return Long.compare(tB, tA);
        });

        int limit = Math.min(docs.size(), 50);
        for (int i = 0; i < limit; i++) {
            DocumentSnapshot doc = docs.get(i);
            String eventType = doc.getString("event_type") != null ? doc.getString("event_type") : "ACTIVIDAD";
            String description = doc.getString("description") != null ? doc.getString("description") : "";
            String fecha = doc.getString("fecha") != null ? doc.getString("fecha") : "";
            historial.add(new CropHistoryItem(eventType, description, fecha));
        }

        return historial;
    }

    public int getAssignedCropId(String deviceId) {
        DocumentSnapshot doc = awaitTask(db.collection("iot_devices").document(deviceId).get());
        if (doc != null && doc.exists() && doc.getLong("assigned_crop_id") != null) {
            return doc.getLong("assigned_crop_id").intValue();
        }
        return -1;
    }

    public void simulateUnassignedCropsData(int userId) {
        QuerySnapshot devicesSnap = awaitTask(db.collection("iot_devices").get());
        List<Integer> cropsWithEsp32 = new ArrayList<>();

        if (devicesSnap != null) {
            for (DocumentSnapshot dev : devicesSnap.getDocuments()) {
                Long assignedId = dev.getLong("assigned_crop_id");
                if (assignedId != null && assignedId > 0) {
                    cropsWithEsp32.add(assignedId.intValue());
                }
            }
        }

        QuerySnapshot cropsSnap = awaitTask(
                db.collection("crops").whereEqualTo("user_id", userId).get()
        );
        if (cropsSnap == null) return;

        java.util.Random random = new java.util.Random();

        for (DocumentSnapshot doc : cropsSnap.getDocuments()) {
            Long idLong = doc.getLong("id");
            if (idLong == null) continue;
            int cropId = idLong.intValue();

            if (cropsWithEsp32.contains(cropId)) {
                continue;
            }

            double currentTemp = doc.getDouble("substrate_temp") != null ? doc.getDouble("substrate_temp") : 23.5;
            double currentMoisture = doc.getDouble("moisture") != null ? doc.getDouble("moisture") : 62.0;
            double optMinT = doc.getDouble("opt_temp_min") != null ? doc.getDouble("opt_temp_min") : 18.0;
            double optMaxT = doc.getDouble("opt_temp_max") != null ? doc.getDouble("opt_temp_max") : 27.0;
            double minM = doc.getDouble("min_moisture") != null ? doc.getDouble("min_moisture") : 40.0;
            double maxM = doc.getDouble("max_moisture") != null ? doc.getDouble("max_moisture") : 80.0;

            double deltaTemp = (random.nextDouble() * 0.8) - 0.4;
            double deltaMoisture = (random.nextDouble() * 2.4) - 1.2;

            double newTemp = Math.round(Math.max(15.0, Math.min(34.0, currentTemp + deltaTemp)) * 10.0) / 10.0;
            double newMoisture = Math.round(Math.max(30.0, Math.min(90.0, currentMoisture + deltaMoisture)) * 10.0) / 10.0;

            String newStatus = (newTemp >= optMinT && newTemp <= optMaxT && newMoisture >= minM && newMoisture <= maxM)
                    ? "Óptimo"
                    : "Atención";

            Map<String, Object> updates = new HashMap<>();
            updates.put("substrate_temp", newTemp);
            updates.put("moisture", newMoisture);
            updates.put("status", newStatus);

            executeVoidTask(
                    db.collection("crops").document(String.valueOf(cropId)).update(updates)
            );
        }
    }
}