package com.example.alerti_back.Model;

import java.util.Date;


public class HistoryEntry {


    private Date timestamp;
    private Double temperature;
    private Double humidity;
    private Double niveauEau;
    private Double vitesseDuVent;

    public HistoryEntry() {
    }





    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getNiveauEau() {
        return niveauEau;
    }

    public void setNiveauEau(Double niveauEau) {
        this.niveauEau = niveauEau;
    }

    public Double getVitesseDuVent() {
        return vitesseDuVent;
    }

    public void setVitesseDuVent(Double vitesseDuVent) {
        this.vitesseDuVent = vitesseDuVent;
    }
}
