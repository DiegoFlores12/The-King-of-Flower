package com.example.flowerking;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DatabaseHelper {

    private static final String TAG = "NeonDB";

    private static final String NEON_CONNECTION_STRING =
            "postgresql://neondb_owner:npg_so57vRJrFXqw@ep-fragrant-boat-b640itrs.c-2.sa-east-1.aws.neon.tech/neondb?sslmode=require";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DatabaseHelper(Context context) {
    }

    private JSONObject executeSql(String query, Object... params) {
        Future<JSONObject> future = executor.submit(() -> {
            HttpURLConnection conn = null;
            try {
                String cleanConnString = NEON_CONNECTION_STRING.trim()
                        .replace("&channel_binding=require", "")
                        .replace("?channel_binding=require", "");

                URI uri = URI.create(cleanConnString);
                String host = uri.getHost();
                if (host == null) return null;

                String endpoint = "https://" + host + "/sql";

                URL url = new URL(endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Neon-Connection-String", cleanConnString);
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("query", query);
                JSONArray jsonParams = new JSONArray();
                if (params != null) {
                    for (Object p : params) {
                        jsonParams.put(p);
                    }
                }
                payload.put("params", jsonParams);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

                StringBuilder response = new StringBuilder();
                if (is != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                    }
                }

                if (code >= 200 && code < 300) {
                    return new JSONObject(response.toString());
                } else {
                    Log.e(TAG, "Error HTTP Neon " + code + ": " + response);
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Excepción conectando a Neon: " + e.getMessage());
                return null;
            } finally {
                if (conn != null) conn.disconnect();
            }
        });

        try {
            return future.get(9, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Timeout o error en Future: " + e.getMessage());
            return null;
        }
    }

    public int registerUser(String username, String email, String password) {
        String sql = "INSERT INTO users (username, email, password) VALUES ($1, $2, $3) RETURNING id;";
        JSONObject res = executeSql(sql, username, email, password);
        if (res != null) {
            JSONArray rows = res.optJSONArray("rows");
            if (rows != null && rows.length() > 0) {
                return rows.optJSONObject(0).optInt("id", -1);
            }
        }
        return -1;
    }

    public int checkLogin(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = $1 AND password = $2 LIMIT 1;";
        JSONObject res = executeSql(sql, username, password);
        if (res != null) {
            JSONArray rows = res.optJSONArray("rows");
            if (rows != null && rows.length() > 0) {
                return rows.optJSONObject(0).optInt("id", -1);
            }
        }
        return -1;
    }

    public List<CropModel> getCropsByUser(int userId) {
        String sql = "SELECT * FROM crops WHERE user_id = $1 ORDER BY id DESC;";
        return parsearCultivos(executeSql(sql, userId));
    }

    public List<CropModel> getAllCrops() {
        String sql = "SELECT * FROM crops ORDER BY id DESC;";
        return parsearCultivos(executeSql(sql));
    }

    private List<CropModel> parsearCultivos(JSONObject res) {
        List<CropModel> list = new ArrayList<>();
        if (res != null) {
            JSONArray rows = res.optJSONArray("rows");
            if (rows != null) {
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.optJSONObject(i);
                    if (row != null) {
                        int id = row.optInt("id");
                        String name = row.optString("name", "Sin nombre");
                        String botanical = row.optString("botanical_name", "Variedad común");
                        String location = row.optString("location", "Sin ubicación");
                        String frame = row.optString("planting_frame", "20 x 20 cm");
                        String density = row.optString("density", "12 plantas/m²");
                        String stage = row.optString("stage", "Vegetativo");
                        String photoPath = row.optString("photo_path", "");
                        double temp = row.optDouble("substrate_temp", 24.0);
                        double moisture = row.optDouble("moisture", 60.0);
                        String status = row.optString("status", "Óptimo");
                        String nextW = row.optString("next_watering", "Hoy 18:00");
                        String freqW = row.optString("watering_freq", "Cada 2 días");

                        CropModel crop = new CropModel(
                                id, name, botanical, stage, location, frame, density, status,
                                String.format(Locale.US, "%.0f°C", temp),
                                String.format(Locale.US, "%.0f%%", moisture),
                                nextW, freqW, photoPath
                        );

                        crop.setMinTemp(row.optDouble("min_temp", 10.0));
                        crop.setOptTempMin(row.optDouble("opt_temp_min", 18.0));
                        crop.setOptTempMax(row.optDouble("opt_temp_max", 27.0));
                        crop.setMaxTemp(row.optDouble("max_temp", 35.0));
                        crop.setMinMoisture(row.optDouble("min_moisture", 40.0));
                        crop.setMaxMoisture(row.optDouble("max_moisture", 80.0));
                        crop.setBotanicalSource(row.optString("botanical_source", "FAO EcoCrop Database"));

                        list.add(crop);
                    }
                }
            }
        }
        return list;
    }

    public long insertCrop(int userId, String name, String botanical, String location, String frame, String stage, String photoPath,
                           double minT, double optMinT, double optMaxT, double maxT, double minM, double maxM, String source, String wateringFreq) {
        String sql = "INSERT INTO crops (user_id, name, botanical_name, location, planting_frame, density, stage, photo_path, " +
                "substrate_temp, moisture, water_level, status, next_watering, watering_freq, " +
                "min_temp, opt_temp_min, opt_temp_max, max_temp, min_moisture, max_moisture, botanical_source) " +
                "VALUES ($1, $2, $3, $4, $5, '12 plantas/m²', $6, $7, 24.0, 65.0, 0.0, 'Óptimo', 'Hoy 20:00', $8, " +
                "$9, $10, $11, $12, $13, $14, $15) RETURNING id;";

        JSONObject res = executeSql(sql, userId, name, botanical, location, frame, stage, photoPath, wateringFreq,
                minT, optMinT, optMaxT, maxT, minM, maxM, source);
        if (res != null) {
            JSONArray rows = res.optJSONArray("rows");
            if (rows != null && rows.length() > 0) {
                return rows.optJSONObject(0).optLong("id", -1);
            }
        }
        return -1;
    }

    public boolean updateCropStage(int id, String newStage) {

        String sql = "UPDATE crops SET stage = $1 " +
                "WHERE id = $2 RETURNING id;";

        JSONObject res = executeSql(sql, newStage, id);

        boolean exito = res != null &&
                res.optInt("rowCount", 0) > 0;

        if (exito) {
            registerCropHistory(
                    id,
                    "ETAPA",
                    "El cultivo cambió a la etapa: " + newStage
            );
        }

        return exito;
    }

    public boolean waterCrop(int id) {

        String sql = "UPDATE crops SET moisture = 78.0, " +
                "status = 'Óptimo', " +
                "next_watering = 'Mañana 18:00' " +
                "WHERE id = $1 RETURNING id;";

        JSONObject res = executeSql(sql, id);

        boolean exito = res != null &&
                res.optInt("rowCount", 0) > 0;

        if (exito) {
            registerCropHistory(
                    id,
                    "RIEGO",
                    "Se realizó un riego manual correctamente."
            );
        }

        return exito;
    }

    public boolean updateIrrigationSchedule(int id, String nextWatering, String wateringFreq) {
        String sql = "UPDATE crops SET next_watering = $1, watering_freq = $2 WHERE id = $3 RETURNING id;";
        JSONObject res = executeSql(sql, nextWatering, wateringFreq, id);
        return res != null && res.optInt("rowCount", 0) > 0;
    }

    public boolean deleteCrop(int id) {
        String sql = "DELETE FROM crops WHERE id = $1 RETURNING id;";
        JSONObject res = executeSql(sql, id);
        return res != null && res.optInt("rowCount", 0) > 0;
    }

    public static class IotAssignmentInfo {
        public int assignedCropId = -1;
        public String assignedCropName = "";
        public boolean isAssignedToThisCrop = false;
    }

    public IotAssignmentInfo checkDeviceAssignment(String deviceId, int currentCropId) {
        IotAssignmentInfo info = new IotAssignmentInfo();
        String sql = "SELECT d.assigned_crop_id, c.name AS crop_name " +
                "FROM iot_devices d " +
                "LEFT JOIN crops c ON d.assigned_crop_id = c.id " +
                "WHERE d.device_id = $1 LIMIT 1;";

        JSONObject res = executeSql(sql, deviceId);
        if (res != null) {
            JSONArray rows = res.optJSONArray("rows");
            if (rows != null && rows.length() > 0) {
                JSONObject row = rows.optJSONObject(0);
                if (row != null && !row.isNull("assigned_crop_id")) {
                    info.assignedCropId = row.optInt("assigned_crop_id", -1);
                    info.assignedCropName = row.optString("crop_name", "Otro cultivo");
                    info.isAssignedToThisCrop = (info.assignedCropId == currentCropId);
                }
            }
        }
        return info;
    }

    public boolean assignDeviceToCrop(String deviceId, int cropId) {
        String sql = "UPDATE iot_devices SET assigned_crop_id = $1 WHERE device_id = $2 RETURNING device_id;";
        JSONObject res = executeSql(sql, cropId, deviceId);
        return res != null && res.optInt("rowCount", 0) > 0;
    }

    public boolean unassignDevice(String deviceId) {
        String sql = "UPDATE iot_devices SET assigned_crop_id = NULL WHERE device_id = $1 RETURNING device_id;";
        JSONObject res = executeSql(sql, deviceId);
        return res != null && res.optInt("rowCount", 0) > 0;
    }

    public boolean registerCropHistory(
            int cropId,
            String eventType,
            String description) {

        String sql = "INSERT INTO crop_history " +
                "(crop_id, event_type, description) " +
                "VALUES ($1, $2, $3) RETURNING id;";

        JSONObject res = executeSql(
                sql,
                cropId,
                eventType,
                description
        );

        return res != null &&
                res.optInt("rowCount", 0) > 0;
    }

    public List<CropHistoryItem> getCropHistory(int cropId) {

        List<CropHistoryItem> historial = new ArrayList<>();

        String sql =
                "SELECT event_type, description, " +
                        "to_char(created_at AT TIME ZONE " +
                        "'America/Santiago', 'DD/MM/YYYY HH24:MI') AS fecha " +
                        "FROM crop_history " +
                        "WHERE crop_id = $1 " +
                        "ORDER BY created_at DESC LIMIT 50;";

        JSONObject res = executeSql(sql, cropId);

        if (res == null) {
            return historial;
        }

        JSONArray rows = res.optJSONArray("rows");

        if (rows == null) {
            return historial;
        }

        for (int i = 0; i < rows.length(); i++) {

            JSONObject row = rows.optJSONObject(i);

            if (row == null) continue;

            CropHistoryItem item = new CropHistoryItem(
                    row.optString("event_type", "ACTIVIDAD"),
                    row.optString("description", ""),
                    row.optString("fecha", "")
            );

            historial.add(item);
        }

        return historial;
    }

    public int getAssignedCropId(String deviceId) {

        String sql =
                "SELECT assigned_crop_id FROM iot_devices " +
                        "WHERE device_id = $1 LIMIT 1;";

        JSONObject res = executeSql(sql, deviceId);

        if (res != null) {

            JSONArray rows = res.optJSONArray("rows");

            if (rows != null && rows.length() > 0) {

                JSONObject row = rows.optJSONObject(0);

                if (row != null && !row.isNull("assigned_crop_id")) {
                    return row.optInt("assigned_crop_id", -1);
                }
            }
        }

        return -1;
    }
}