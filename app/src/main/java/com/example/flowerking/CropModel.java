package com.example.flowerking;

import java.io.Serializable;

public class CropModel implements Serializable {
    private int id;
    private String name;
    private String botanicalName;
    private String stage;
    private String location;
    private String plantingFrame;
    private String density;
    private String status;
    private String substrateTemp;
    private String moisture;
    private String nextWatering;
    private String wateringFreq;
    private String photoPath;

    public CropModel(int id, String name, String botanicalName, String stage, String location,
                     String plantingFrame, String density, String status,
                     String substrateTemp, String moisture, String nextWatering, String wateringFreq, String photoPath) {
        this.id = id;
        this.name = name;
        this.botanicalName = botanicalName;
        this.stage = stage;
        this.location = location;
        this.plantingFrame = plantingFrame;
        this.density = density;
        this.status = status;
        this.substrateTemp = substrateTemp;
        this.moisture = moisture;
        this.nextWatering = nextWatering;
        this.wateringFreq = wateringFreq;
        this.photoPath = photoPath;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getBotanicalName() { return botanicalName; }
    public String getStage() { return stage; }
    public String getLocation() { return location; }
    public String getPlantingFrame() { return plantingFrame; }
    public String getDensity() { return density; }
    public String getStatus() { return status; }
    public String getSubstrateTemp() { return substrateTemp; }
    public String getMoisture() { return moisture; }
    public String getNextWatering() { return nextWatering; }
    public String getWateringFreq() { return wateringFreq; }
    public String getPhotoPath() { return photoPath; }

    public void setStage(String stage) { this.stage = stage; }
    public void setStatus(String status) { this.status = status; }
    public void setMoisture(String moisture) { this.moisture = moisture; }
}