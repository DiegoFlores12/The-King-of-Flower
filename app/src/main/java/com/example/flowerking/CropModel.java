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

    private double minTemp = 10.0;
    private double optTempMin = 18.0;
    private double optTempMax = 27.0;
    private double maxTemp = 35.0;
    private double minMoisture = 40.0;
    private double maxMoisture = 80.0;
    private String botanicalSource = "FAO EcoCrop";

    public CropModel(int id, String name, String botanicalName, String stage, String location,
                     String plantingFrame, String density, String status, String substrateTemp,
                     String moisture, String nextWatering, String wateringFreq, String photoPath) {
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
    public void setStage(String stage) { this.stage = stage; }
    public String getLocation() { return location; }
    public String getPlantingFrame() { return plantingFrame; }
    public String getDensity() { return density; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSubstrateTemp() { return substrateTemp; }
    public void setSubstrateTemp(String substrateTemp) { this.substrateTemp = substrateTemp; }
    public String getMoisture() { return moisture; }
    public void setMoisture(String moisture) { this.moisture = moisture; }
    public String getNextWatering() { return nextWatering; }
    public void setNextWatering(String nextWatering) { this.nextWatering = nextWatering; }
    public String getWateringFreq() { return wateringFreq; }
    public String getPhotoPath() { return photoPath; }

    public double getMinTemp() { return minTemp; }
    public void setMinTemp(double minTemp) { this.minTemp = minTemp; }

    public double getOptTempMin() { return optTempMin; }
    public void setOptTempMin(double optTempMin) { this.optTempMin = optTempMin; }

    public double getOptTempMax() { return optTempMax; }
    public void setOptTempMax(double optTempMax) { this.optTempMax = optTempMax; }

    public double getMaxTemp() { return maxTemp; }
    public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }

    public double getMinMoisture() { return minMoisture; }
    public void setMinMoisture(double minMoisture) { this.minMoisture = minMoisture; }

    public double getMaxMoisture() { return maxMoisture; }
    public void setMaxMoisture(double maxMoisture) { this.maxMoisture = maxMoisture; }

    public String getBotanicalSource() { return botanicalSource; }
    public void setBotanicalSource(String botanicalSource) { this.botanicalSource = botanicalSource; }
}