-- Script SQL pour créer la table des signalements SOS/urgence des citoyens
-- À exécuter dans Supabase

-- Activer l'extension PostGIS pour les fonctions géographiques
CREATE EXTENSION IF NOT EXISTS postgis;

-- Créer la table sos_signals
CREATE TABLE IF NOT EXISTS sos_signals (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL DEFAULT 0, -- 0 pour les signalements anonymes
    type_urgence VARCHAR(50) NOT NULL, -- Type d'urgence (inondation, accident, etc.)
    description TEXT NOT NULL, -- Description détaillée
    localite VARCHAR(100) NOT NULL, -- Localité
    latitude DECIMAL(10, 8) NOT NULL, -- Coordonnées GPS
    longitude DECIMAL(11, 8) NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'en_cours', -- en_cours, traite, resolu
    priorite VARCHAR(20) NOT NULL DEFAULT 'moyenne', -- faible, moyenne, haute, critique
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), -- Date de création
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), -- Dernière mise à jour
    photo_url TEXT, -- URL de photo si disponible
    numero_urgence VARCHAR(20), -- Numéro de téléphone d'urgence
    anonyme BOOLEAN NOT NULL DEFAULT FALSE, -- Signalement anonyme
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() -- Date de création système
);

-- Créer des index pour optimiser les requêtes
CREATE INDEX IF NOT EXISTS idx_sos_signals_user_id ON sos_signals(user_id);
CREATE INDEX IF NOT EXISTS idx_sos_signals_statut ON sos_signals(statut);
CREATE INDEX IF NOT EXISTS idx_sos_signals_priorite ON sos_signals(priorite);
CREATE INDEX IF NOT EXISTS idx_sos_signals_timestamp ON sos_signals(signal_timestamp);
CREATE INDEX IF NOT EXISTS idx_sos_signals_localite ON sos_signals(localite);
CREATE INDEX IF NOT EXISTS idx_sos_signals_type_urgence ON sos_signals(type_urgence);

-- Créer un index géographique pour les recherches par proximité
CREATE INDEX IF NOT EXISTS idx_sos_signals_location ON sos_signals USING GIST (
    ST_Point(longitude::double precision, latitude::double precision)
);

-- Fonction pour mettre à jour automatiquement updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger pour mettre à jour updated_at automatiquement
DROP TRIGGER IF EXISTS update_sos_signals_updated_at ON sos_signals;
CREATE TRIGGER update_sos_signals_updated_at
    BEFORE UPDATE ON sos_signals
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Créer une vue pour les signalements avec informations utilisateur (si non anonyme)
CREATE OR REPLACE VIEW sos_signals_with_user AS
SELECT 
    s.*,
    CASE 
        WHEN s.anonyme OR s.user_id = 0 THEN 'Anonyme'
        ELSE u.prenom || ' ' || u.nom
    END as nom_utilisateur,
    CASE 
        WHEN s.anonyme OR s.user_id = 0 THEN NULL
        ELSE u.email
    END as email_utilisateur,
    CASE 
        WHEN s.anonyme OR s.user_id = 0 THEN NULL
        ELSE u.num_tel
    END as telephone_utilisateur
FROM sos_signals s
LEFT JOIN users u ON s.user_id = u.id AND NOT s.anonyme;

-- Créer une fonction pour obtenir les signalements dans un rayon donné
CREATE OR REPLACE FUNCTION get_sos_signals_in_radius(
    center_lat DECIMAL,
    center_lon DECIMAL,
    radius_km DECIMAL DEFAULT 10
)
RETURNS TABLE (
    id INTEGER,
    type_urgence VARCHAR,
    description TEXT,
    localite VARCHAR,
    latitude DECIMAL,
    longitude DECIMAL,
    statut VARCHAR,
    priorite VARCHAR,
    signal_timestamp TIMESTAMP WITH TIME ZONE,
    distance_km DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.id,
        s.type_urgence,
        s.description,
        s.localite,
        s.latitude,
        s.longitude,
        s.statut,
        s.priorite,
        s.signal_timestamp,
        ROUND(
            ST_Distance(
                ST_Point(s.longitude::double precision, s.latitude::double precision)::geography,
                ST_Point(center_lon::double precision, center_lat::double precision)::geography
            ) / 1000, 2
        ) as distance_km
    FROM sos_signals s
    WHERE ST_DWithin(
        ST_Point(s.longitude::double precision, s.latitude::double precision)::geography,
        ST_Point(center_lon::double precision, center_lat::double precision)::geography,
        radius_km * 1000
    )
    ORDER BY distance_km ASC;
END;
$$ LANGUAGE plpgsql;

-- Insérer des données de test (optionnel)
INSERT INTO sos_signals (
    user_id, type_urgence, description, localite, latitude, longitude, 
    statut, priorite, anonyme
) VALUES 
(1, 'inondation', 'Rue inondée après la pluie, circulation impossible', 'Yirimadio', 14.6928, -17.4467, 'en_cours', 'haute', false),
(0, 'accident', 'Accident de voiture sur la route, blessés sur place', 'Autogare', 14.7886, -16.9260, 'en_cours', 'critique', true),
(2, 'infrastructure', 'Effondrement de la route', 'Niamana', 16.0320, -16.4902, 'traite', 'moyenne', false)
ON CONFLICT DO NOTHING;

-- Créer des politiques RLS (Row Level Security) pour Supabase
ALTER TABLE sos_signals ENABLE ROW LEVEL SECURITY;

-- Politique pour permettre la lecture à tous (signalements publics)
CREATE POLICY "Allow public read access to sos_signals" ON sos_signals
    FOR SELECT USING (true);

-- Politique pour permettre l'insertion à tous (signalements anonymes autorisés)
CREATE POLICY "Allow public insert access to sos_signals" ON sos_signals
    FOR INSERT WITH CHECK (true);

-- Politique pour permettre la mise à jour seulement aux utilisateurs authentifiés
CREATE POLICY "Allow authenticated users to update their own sos_signals" ON sos_signals
    FOR UPDATE USING (
        auth.uid() IS NOT NULL AND 
        (user_id = (SELECT id FROM users WHERE auth.uid()::text = email) OR 
         user_id = 0)
    );

-- Commentaires sur la table et les colonnes
COMMENT ON TABLE sos_signals IS 'Table des signalements SOS/urgence des citoyens';
COMMENT ON COLUMN sos_signals.user_id IS 'ID de l''utilisateur (0 pour anonyme)';
COMMENT ON COLUMN sos_signals.type_urgence IS 'Type d''urgence: inondation, accident, incendie, etc.';
COMMENT ON COLUMN sos_signals.statut IS 'Statut: en_cours, traite, resolu';
COMMENT ON COLUMN sos_signals.priorite IS 'Priorité: faible, moyenne, haute, critique';
COMMENT ON COLUMN sos_signals.anonyme IS 'Indique si le signalement est anonyme';

-- Afficher un message de confirmation
DO $$
BEGIN
    RAISE NOTICE 'Table sos_signals créée avec succès!';
    RAISE NOTICE 'Index et triggers configurés.';
    RAISE NOTICE 'Vue sos_signals_with_user créée.';
    RAISE NOTICE 'Fonction get_sos_signals_in_radius créée.';
    RAISE NOTICE 'Politiques RLS configurées.';
    RAISE NOTICE 'Données de test insérées (si pas de conflit).';
END $$;
