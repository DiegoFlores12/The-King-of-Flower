package com.example.flowerking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "flowerking_local.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "email TEXT NOT NULL, " +
                "password TEXT NOT NULL);");

        db.execSQL("CREATE TABLE crops (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "name TEXT NOT NULL, " +
                "botanical_name TEXT, " +
                "location TEXT, " +
                "planting_frame TEXT, " +
                "density TEXT DEFAULT '12 plantas/m²', " +
                "stage TEXT, " +
                "photo_path TEXT, " +
                "substrate_temp REAL DEFAULT 24.0, " +
                "moisture REAL DEFAULT 65.0, " +
                "water_level REAL DEFAULT 0.0, " +
                "status TEXT DEFAULT 'Óptimo', " +
                "next_watering TEXT DEFAULT 'Hoy 20:00', " +
                "watering_freq TEXT DEFAULT 'Cada 2 días', " +
                "min_temp REAL, " +
                "opt_temp_min REAL, " +
                "opt_temp_max REAL, " +
                "max_temp REAL, " +
                "min_moisture REAL, " +
                "max_moisture REAL, " +
                "botanical_source TEXT);");

        db.execSQL("CREATE TABLE crop_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "crop_id INTEGER, " +
                "event_type TEXT, " +
                "description TEXT, " +
                "fecha TEXT);");

        db.execSQL("CREATE TABLE iot_devices (" +
                "device_id TEXT PRIMARY KEY, " +
                "assigned_crop_id INTEGER);");

        db.execSQL("INSERT INTO users (username, email, password) " +
                "VALUES ('diego', 'diego@flowerking.cl', 'diego123');");

        db.execSQL("INSERT INTO crops (" +
                "user_id, name, botanical_name, location, planting_frame, density, stage, photo_path, " +
                "substrate_temp, moisture, water_level, status, next_watering, watering_freq, " +
                "min_temp, opt_temp_min, opt_temp_max, max_temp, min_moisture, max_moisture, botanical_source) " +
                "VALUES (1, 'Tomate Cherokee', 'Solanum lycopersicum', 'Bancal A', '40x40 cm', '12 plantas/m²', " +
                "'Crecimiento Vegetativo', '', 23.5, 62.0, 0.0, 'Óptimo', 'Hoy 20:00', 'Cada 2 días', " +
                "10.0, 18.0, 27.0, 35.0, 40.0, 80.0, 'FAO EcoCrop Database');");

        db.execSQL("INSERT INTO crops (" +
                "user_id, name, botanical_name, location, planting_frame, density, stage, photo_path, " +
                "substrate_temp, moisture, water_level, status, next_watering, watering_freq, " +
                "min_temp, opt_temp_min, opt_temp_max, max_temp, min_moisture, max_moisture, botanical_source) " +
                "VALUES (1, 'Albahaca Genovesa', 'Ocimum basilicum', 'Maceta B', '20x20 cm', '16 plantas/m²', " +
                "'Floración', '', 21.0, 70.0, 0.0, 'Óptimo', 'Mañana 18:00', 'Cada 2 días', " +
                "12.0, 20.0, 28.0, 35.0, 50.0, 85.0, 'FAO EcoCrop Database');");

        db.execSQL("INSERT INTO iot_devices (device_id, assigned_crop_id) VALUES ('ESP32_NODE_01', 1);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS iot_devices;");
        db.execSQL("DROP TABLE IF EXISTS crop_history;");
        db.execSQL("DROP TABLE IF EXISTS crops;");
        db.execSQL("DROP TABLE IF EXISTS users;");
        onCreate(db);
    }

    public int registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);
        long res = db.insert("users", null, values);
        return (int) res;
    }

    public int checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ? AND password = ? LIMIT 1",
                new String[]{username, password});
        int id = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
            cursor.close();
        }
        return id;
    }

    public List<CropModel> getCropsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM crops WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)});
        return parsearCultivos(cursor);
    }

    public List<CropModel> getAllCrops() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM crops ORDER BY id DESC", null);
        return parsearCultivos(cursor);
    }

    private List<CropModel> parsearCultivos(Cursor cursor) {
        List<CropModel> list = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String botanical = cursor.getString(cursor.getColumnIndexOrThrow("botanical_name"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                String frame = cursor.getString(cursor.getColumnIndexOrThrow("planting_frame"));
                String density = cursor.getString(cursor.getColumnIndexOrThrow("density"));
                String stage = cursor.getString(cursor.getColumnIndexOrThrow("stage"));
                String photoPath = cursor.getString(cursor.getColumnIndexOrThrow("photo_path"));
                double temp = cursor.getDouble(cursor.getColumnIndexOrThrow("substrate_temp"));
                double moisture = cursor.getDouble(cursor.getColumnIndexOrThrow("moisture"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String nextW = cursor.getString(cursor.getColumnIndexOrThrow("next_watering"));
                String freqW = cursor.getString(cursor.getColumnIndexOrThrow("watering_freq"));

                CropModel crop = new CropModel(
                        id, name, botanical, stage, location, frame, density, status,
                        String.format(Locale.US, "%.0f°C", temp),
                        String.format(Locale.US, "%.0f%%", moisture),
                        nextW, freqW, photoPath
                );

                crop.setMinTemp(cursor.getDouble(cursor.getColumnIndexOrThrow("min_temp")));
                crop.setOptTempMin(cursor.getDouble(cursor.getColumnIndexOrThrow("opt_temp_min")));
                crop.setOptTempMax(cursor.getDouble(cursor.getColumnIndexOrThrow("opt_temp_max")));
                crop.setMaxTemp(cursor.getDouble(cursor.getColumnIndexOrThrow("max_temp")));
                crop.setMinMoisture(cursor.getDouble(cursor.getColumnIndexOrThrow("min_moisture")));
                crop.setMaxMoisture(cursor.getDouble(cursor.getColumnIndexOrThrow("max_moisture")));
                crop.setBotanicalSource(cursor.getString(cursor.getColumnIndexOrThrow("botanical_source")));

                list.add(crop);
            }
            cursor.close();
        }
        return list;
    }

    public long insertCrop(int userId, String name, String botanical, String location, String frame, String stage, String photoPath,
                           double minT, double optMinT, double optMaxT, double maxT, double minM, double maxM, String source, String wateringFreq) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("name", name);
        values.put("botanical_name", botanical);
        values.put("location", location);
        values.put("planting_frame", frame);
        values.put("density", "12 plantas/m²");
        values.put("stage", stage);
        values.put("photo_path", photoPath);
        values.put("substrate_temp", 24.0);
        values.put("moisture", 65.0);
        values.put("water_level", 0.0);
        values.put("status", "Óptimo");
        values.put("next_watering", "Hoy 20:00");
        values.put("watering_freq", wateringFreq);
        values.put("min_temp", minT);
        values.put("opt_temp_min", optMinT);
        values.put("opt_temp_max", optMaxT);
        values.put("max_temp", maxT);
        values.put("min_moisture", minM);
        values.put("max_moisture", maxM);
        values.put("botanical_source", source);
        return db.insert("crops", null, values);
    }

    public boolean updateCropStage(int id, String newStage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("stage", newStage);
        int rows = db.update("crops", values, "id = ?", new String[]{String.valueOf(id)});
        if (rows > 0) {
            registerCropHistory(id, "ETAPA", "El cultivo cambió a la etapa: " + newStage);
            return true;
        }
        return false;
    }

    public boolean waterCrop(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("moisture", 78.0);
        values.put("status", "Óptimo");
        values.put("next_watering", "Mañana 18:00");
        int rows = db.update("crops", values, "id = ?", new String[]{String.valueOf(id)});
        if (rows > 0) {
            registerCropHistory(id, "RIEGO", "Se realizó un riego manual correctamente.");
            return true;
        }
        return false;
    }

    public boolean updateIrrigationSchedule(int id, String nextWatering, String wateringFreq) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("next_watering", nextWatering);
        values.put("watering_freq", wateringFreq);
        int rows = db.update("crops", values, "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteCrop(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("crops", "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public static class IotAssignmentInfo {
        public int assignedCropId = -1;
        public String assignedCropName = "";
        public boolean isAssignedToThisCrop = false;
    }

    public IotAssignmentInfo checkDeviceAssignment(String deviceId, int currentCropId) {
        IotAssignmentInfo info = new IotAssignmentInfo();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT d.assigned_crop_id, c.name AS crop_name " +
                "FROM iot_devices d " +
                "LEFT JOIN crops c ON d.assigned_crop_id = c.id " +
                "WHERE d.device_id = ? LIMIT 1", new String[]{deviceId});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int colCropId = cursor.getColumnIndex("assigned_crop_id");
                int colCropName = cursor.getColumnIndex("crop_name");
                if (colCropId != -1 && !cursor.isNull(colCropId)) {
                    info.assignedCropId = cursor.getInt(colCropId);
                    info.assignedCropName = (colCropName != -1 && !cursor.isNull(colCropName)) ? cursor.getString(colCropName) : "Otro cultivo";
                    info.isAssignedToThisCrop = (info.assignedCropId == currentCropId);
                }
            }
            cursor.close();
        }
        return info;
    }

    public boolean assignDeviceToCrop(String deviceId, int cropId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("device_id", deviceId);
        values.put("assigned_crop_id", cropId);
        long res = db.insertWithOnConflict("iot_devices", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return res != -1;
    }

    public boolean unassignDevice(String deviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull("assigned_crop_id");
        int rows = db.update("iot_devices", values, "device_id = ?", new String[]{deviceId});
        return rows > 0;
    }

    public boolean registerCropHistory(int cropId, String eventType, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fecha = sdf.format(new Date());

        ContentValues values = new ContentValues();
        values.put("crop_id", cropId);
        values.put("event_type", eventType);
        values.put("description", description);
        values.put("fecha", fecha);
        long res = db.insert("crop_history", null, values);
        return res != -1;
    }

    public List<CropHistoryItem> getCropHistory(int cropId) {
        List<CropHistoryItem> historial = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT event_type, description, fecha FROM crop_history WHERE crop_id = ? ORDER BY id DESC LIMIT 50",
                new String[]{String.valueOf(cropId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String eventType = cursor.getString(0);
                String description = cursor.getString(1);
                String fecha = cursor.getString(2);
                historial.add(new CropHistoryItem(eventType, description, fecha));
            }
            cursor.close();
        }
        return historial;
    }

    public int getAssignedCropId(String deviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT assigned_crop_id FROM iot_devices WHERE device_id = ? LIMIT 1",
                new String[]{deviceId});
        int result = -1;
        if (cursor != null) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                result = cursor.getInt(0);
            }
            cursor.close();
        }
        return result;
    }
}