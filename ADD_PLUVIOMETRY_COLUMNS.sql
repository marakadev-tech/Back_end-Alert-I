-- Script SQL pour ajouter les colonnes pluviométrie à la table sensors
-- Exécutez ce script dans votre console Supabase SQL

-- Ajouter les colonnes pluviométrie si elles n'existent pas
ALTER TABLE sensors 
ADD COLUMN IF NOT EXISTS pluviometrie_jour DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS seuil_pluviometrie DOUBLE PRECISION;

-- Ajouter des commentaires pour documenter les colonnes
COMMENT ON COLUMN sensors.pluviometrie_jour IS 'Pluviométrie du jour en mm (données OpenWeatherMap)';
COMMENT ON COLUMN sensors.seuil_pluviometrie IS 'Seuil pluviométrique en mm/jour pour déclencher les alertes';

-- Mettre à jour les seuils pluviométriques pour les capteurs existants
-- (Remplacez par vos valeurs calculées)
UPDATE sensors 
SET seuil_pluviometrie = 25.95 
WHERE localite = 'sebenikoro' AND seuil_pluviometrie IS NULL;

UPDATE sensors 
SET seuil_pluviometrie = 30.0 
WHERE localite = 'bamako' AND seuil_pluviometrie IS NULL;

-- Vérifier que les colonnes ont été ajoutées
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'sensors' 
AND column_name IN ('pluviometrie_jour', 'seuil_pluviometrie');

-- Afficher les capteurs avec leurs seuils
SELECT id, localite, latitude, longitude, seuil_pluviometrie 
FROM sensors 
WHERE statut = 'active';
