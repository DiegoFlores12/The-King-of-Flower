package com.example.flowerking;

public class CropHistoryItem {

    private final String eventType;
    private final String description;
    private final String date;

    public CropHistoryItem(
            String eventType,
            String description,
            String date) {

        this.eventType = eventType;
        this.description = description;
        this.date = date;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }
}