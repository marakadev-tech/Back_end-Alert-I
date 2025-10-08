package com.example.alerti_back.Model;

import java.util.Date;
import java.util.List;

public class Sensors {
    private String id;
    private String statut;
    private String localite;
    private Double latitude;
    private Double longitude;
    private Double dernierDonneeCaptemperature;

    private Double dernierDonneeCapniveauEau;
    private Double dernierDonneevitesseDuVent;
    private Double seuilniveauEau;
    
    // Champs pour la pluviométrie
    private Double pluviometrieJour;  // Pluviométrie du jour en mm
    private Double seuilPluviometrie; // Seuil pluviométrique en mm
    private Date timestamp;     // Date et heure de la dernière requête du capteur
    private Date updatedAt;     // Date de mise à jour du document
    private List<HistoryEntry> history;  // Historique des mesures du capteur

    // Getters et Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getLocalite() {
        return localite;
    }

    public void setLocalite(String localite) {
        this.localite = localite;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDernierDonneeCaptemperature() {
        return dernierDonneeCaptemperature;
    }

    public void setDernierDonneeCaptemperature(Double dernierDonneeCaptemperature) {
        this.dernierDonneeCaptemperature = dernierDonneeCaptemperature;
    }



    public Double getDernierDonneeCapniveauEau() {
        return dernierDonneeCapniveauEau;
    }

    public void setDernierDonneeCapniveauEau(Double dernierDonneeCapniveauEau) {
        this.dernierDonneeCapniveauEau = dernierDonneeCapniveauEau;
    }

    public Double getDernierDonneevitesseDuVent() {
        return dernierDonneevitesseDuVent;
    }

    public void setDernierDonneevitesseDuVent(Double dernierDonneevitesseDuVent) {
        this.dernierDonneevitesseDuVent = dernierDonneevitesseDuVent;
    }

    public Double getSeuilniveauEau() {
        return seuilniveauEau;
    }

    public void setSeuilniveauEau(Double seuilniveauEau) {
        this.seuilniveauEau = seuilniveauEau;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryEntry> history) {
        this.history = history;
    }

    // Getters et Setters pour la pluviométrie
    public Double getPluviometrieJour() {
        return pluviometrieJour;
    }

    public void setPluviometrieJour(Double pluviometrieJour) {
        this.pluviometrieJour = pluviometrieJour;
    }

    public Double getSeuilPluviometrie() {
        return seuilPluviometrie;
    }

    public void setSeuilPluviometrie(Double seuilPluviometrie) {
        this.seuilPluviometrie = seuilPluviometrie;
    }

}
