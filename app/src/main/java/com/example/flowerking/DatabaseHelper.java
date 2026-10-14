package com.example.flowerking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "flower_king.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
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
                "user_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "botanical_name TEXT DEFAULT 'Variedad común', " +
                "location TEXT NOT NULL, " +
                "planting_frame TEXT DEFAULT '20 x 20 cm', " +
                "density TEXT DEFAULT '12 plantas/m²', " +
                "stage TEXT DEFAULT 'Vegetativo', " +
                "photo_path TEXT DEFAULT '', " +
                "substrate_temp REAL DEFAULT 24.0, " +
                "moisture REAL DEFAULT 60.0, " +
                "status TEXT DEFAULT 'Óptimo', " +
                "next_watering TEXT DEFAULT 'Hoy 18:00', " +
                "watering_freq TEXT DEFAULT 'Cada 2 días (500 ml)', " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);");

        ContentValues userCv = new ContentValues();
        userCv.put("username", "diego");
        userCv.put("email", "diego@flowerking.cl");
        userCv.put("password", "1234");
        long userId = db.insert("users", null, userCv);

        if (userId != -1) {
            insertarCultivoInicial(db, (int) userId, "Tomate Cherry", "Solanum lycopersicum", "Bancal A - Línea 1", "20 x 30 cm", "Fructificación", 23.0, 62.0, "Óptimo", "Hoy 18:00", "Cada 2 días (600 ml)");
            insertarCultivoInicial(db, (int) userId, "Albahaca Genovesa", "Ocimum basilicum", "Módulo 2 - Bandeja B", "15 x 20 cm", "Vegetativo", 25.0, 36.0, "Riego requerido", "En 30 minutos", "Diario (250 ml)");
        }
    }

    private void insertarCultivoInicial(SQLiteDatabase db, int userId, String name, String botanical, String loc, String frame, String stage, double temp, double moisture, String status, String nextW, String freqW) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("name", name);
        cv.put("botanical_name", botanical);
        cv.put("location", loc);
        cv.put("planting_frame", frame);
        cv.put("density", "12 plantas/m²");
        cv.put("stage", stage);
        cv.put("photo_path", "");
        cv.put("substrate_temp", temp);
        cv.put("moisture", moisture);
        cv.put("status", status);
        cv.put("next_watering", nextW);
        cv.put("watering_freq", freqW);
        db.insert("crops", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS crops");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public int registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("email", email);
        cv.put("password", password);

        long id = db.insert("users", null, cv);
        return (int) id;
    }

    public int checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ? AND password = ?", new String[]{username, password});
        int userId = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            }
            cursor.close();
        }
        return userId;
    }

    public List<CropModel> getCropsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM crops WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)});
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
            if (cursor.moveToFirst()) {
                do {
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

                    list.add(new CropModel(
                            id, name, botanical, stage, location, frame, density, status,
                            String.format(Locale.US, "%.0f°C", temp),
                            String.format(Locale.US, "%.0f%%", moisture),
                            nextW, freqW, photoPath
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public long insertCrop(int userId, String name, String botanical, String location, String frame, String stage, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("name", name);
        cv.put("botanical_name", botanical);
        cv.put("location", location);
        cv.put("planting_frame", frame);
        cv.put("density", "12 plantas/m²");
        cv.put("stage", stage);
        cv.put("photo_path", photoPath);
        cv.put("substrate_temp", 24.0);
        cv.put("moisture", 65.0);
        cv.put("status", "Óptimo");
        cv.put("next_watering", "Hoy 20:00");
        cv.put("watering_freq", "Cada 2 días (500 ml)");

        return db.insert("crops", null, cv);
    }

    public boolean updateCropStage(int id, String newStage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("stage", newStage);
        return db.update("crops", cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean waterCrop(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("moisture", 78.0);
        cv.put("status", "Óptimo");
        cv.put("next_watering", "Mañana 18:00");
        return db.update("crops", cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateIrrigationSchedule(int id, String nextWatering, String wateringFreq) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("next_watering", nextWatering);
        cv.put("watering_freq", wateringFreq);
        return db.update("crops", cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteCrop(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("crops", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }
}