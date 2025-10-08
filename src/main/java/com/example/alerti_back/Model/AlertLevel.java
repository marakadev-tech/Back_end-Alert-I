package com.example.alerti_back.Model;

/**
 * Énumération des niveaux d'alerte basés sur le niveau d'eau
 */
public enum AlertLevel {
    /**
     * Niveau normal - Niveau d'eau < 60% du seuil
     */
    NORMAL("Normal", "Le niveau d'eau est normal", 0.0, 0.60),
    
    /**
     * Niveau attention - Niveau d'eau entre 60% et 85% du seuil
     */
    ATTENTION("Attention", "Le niveau d'eau nécessite une surveillance", 0.60, 0.85),
    
    /**
     * Niveau danger - Niveau d'eau >= 85% du seuil
     */
    DANGER("Danger", "Le niveau d'eau est critique, risque d'inondation", 0.85, Double.MAX_VALUE);

    private final String label;
    private final String description;
    private final double minRatio;  // Ratio minimum par rapport au seuil
    private final double maxRatio;  // Ratio maximum par rapport au seuil

    AlertLevel(String label, String description, double minRatio, double maxRatio) {
        this.label = label;
        this.description = description;
        this.minRatio = minRatio;
        this.maxRatio = maxRatio;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public double getMinRatio() {
        return minRatio;
    }

    public double getMaxRatio() {
        return maxRatio;
    }

    /**
     * Détermine le niveau d'alerte en fonction du niveau d'eau actuel et du seuil
     * @param niveauEau Niveau d'eau actuel
     * @param seuilEau Seuil d'eau critique
     * @return Le niveau d'alerte correspondant
     */
    public static AlertLevel determinerNiveau(Double niveauEau, Double seuilEau) {
        if (niveauEau == null || seuilEau == null || seuilEau == 0) {
            return NORMAL;
        }

        double ratio = niveauEau / seuilEau;

        for (AlertLevel level : values()) {
            if (ratio >= level.minRatio && ratio < level.maxRatio) {
                return level;
            }
        }

        return DANGER; // Par défaut si ratio très élevé
    }

    /**
     * Vérifie si le niveau d'alerte nécessite une notification
     * @return true si le niveau nécessite une notification
     */
    public boolean necessiteNotification() {
        return this == ATTENTION || this == DANGER;
    }
}
