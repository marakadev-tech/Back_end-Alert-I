package com.example.alerti_back.Model;

import java.util.Date;

/**
 * Modèle pour stocker les tokens FCM des utilisateurs
 */
public class FcmToken {
    private String id;
    private String userId;
    private String fcmToken;
    private String deviceType; // "android", "ios", "web"
    private String deviceId;
    private String localite; // Localité d'intérêt de l'utilisateur
    private Boolean isActive;
    private Date createdAt;
    private Date updatedAt;
    private Date lastUsed;

    // Constructeurs
    public FcmToken() {}

    public FcmToken(String userId, String fcmToken, String deviceType, String localite) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.localite = localite;
        this.isActive = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.lastUsed = new Date();
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLocalite() {
        return localite;
    }

    public void setLocalite(String localite) {
        this.localite = localite;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }
}
