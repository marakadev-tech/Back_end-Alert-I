-- Script SQL pour créer la table fcm_tokens dans Supabase
-- Exécutez ce script dans l'éditeur SQL de Supabase

-- Créer la table fcm_tokens
CREATE TABLE IF NOT EXISTS fcm_tokens (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    fcm_token TEXT NOT NULL UNIQUE,
    device_type VARCHAR(50) DEFAULT 'unknown', -- 'android', 'ios', 'web', 'unknown'
    device_id VARCHAR(255),
    localite VARCHAR(255) NOT NULL, -- Localité d'intérêt de l'utilisateur
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_used TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Créer un index sur fcm_token pour les recherches rapides
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_token ON fcm_tokens(fcm_token);

-- Créer un index sur user_id pour les recherches par utilisateur
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON fcm_tokens(user_id);

-- Créer un index sur localite pour les recherches par localité
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_localite ON fcm_tokens(localite);

-- Créer un index sur is_active pour filtrer les tokens actifs
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_is_active ON fcm_tokens(is_active);

-- Créer une fonction pour mettre à jour automatiquement updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Créer un trigger pour mettre à jour updated_at automatiquement
DROP TRIGGER IF EXISTS update_fcm_tokens_updated_at ON fcm_tokens;
CREATE TRIGGER update_fcm_tokens_updated_at
    BEFORE UPDATE ON fcm_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Activer Row Level Security (RLS) pour la sécurité
ALTER TABLE fcm_tokens ENABLE ROW LEVEL SECURITY;

-- Créer une politique pour permettre l'accès en lecture/écriture avec la clé de service
-- (Cette politique permet à votre backend Spring Boot d'accéder aux données)
CREATE POLICY "Service role can manage fcm_tokens" ON fcm_tokens
    FOR ALL USING (true);

-- Insérer quelques données de test (optionnel)
-- Décommentez les lignes suivantes pour insérer des données de test
/*
INSERT INTO fcm_tokens (user_id, fcm_token, device_type, localite) VALUES
('user_001', 'test_token_android_001', 'android', 'Dakar'),
('user_002', 'test_token_ios_001', 'ios', 'Dakar'),
('user_003', 'test_token_android_002', 'android', 'Thiès'),
('user_004', 'test_token_web_001', 'web', 'Dakar');
*/

-- Vérifier que la table a été créée correctement
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'fcm_tokens' 
ORDER BY ordinal_position;
