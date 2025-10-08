-- Script SQL complet pour configurer les tokens FCM
-- Exécutez ce script dans l'éditeur SQL de Supabase

-- 1. Créer la table fcm_tokens
CREATE TABLE IF NOT EXISTS fcm_tokens (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id INTEGER NOT NULL,
    fcm_token TEXT NOT NULL UNIQUE,
    device_type VARCHAR(50) DEFAULT 'unknown',
    device_id VARCHAR(255),
    localite VARCHAR(255) NOT NULL DEFAULT 'Mali',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_used TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT fk_fcm_tokens_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- 2. Créer les index
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_token ON fcm_tokens(fcm_token);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON fcm_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_localite ON fcm_tokens(localite);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_is_active ON fcm_tokens(is_active);

-- 3. Créer la fonction de mise à jour automatique
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 4. Créer le trigger
DROP TRIGGER IF EXISTS update_fcm_tokens_updated_at ON fcm_tokens;
CREATE TRIGGER update_fcm_tokens_updated_at
    BEFORE UPDATE ON fcm_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 5. Activer RLS
ALTER TABLE fcm_tokens ENABLE ROW LEVEL SECURITY;

-- 6. Créer les politiques RLS
CREATE POLICY "Service role can manage fcm_tokens" ON fcm_tokens
    FOR ALL USING (true);

-- 7. Créer la fonction principale pour enregistrer les tokens
CREATE OR REPLACE FUNCTION upsert_fcm_token_simple(
    p_user_id INTEGER,
    p_fcm_token TEXT,
    p_device_type VARCHAR(50) DEFAULT 'android',
    p_device_id VARCHAR(255) DEFAULT NULL,
    p_localite VARCHAR(255) DEFAULT 'Mali'
)
RETURNS JSON AS $$
DECLARE
    result JSON;
    token_id UUID;
BEGIN
    -- Vérifier si l'utilisateur existe
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = p_user_id) THEN
        RETURN json_build_object(
            'success', false,
            'message', 'Utilisateur non trouvé'
        );
    END IF;

    -- Insérer ou mettre à jour le token
    INSERT INTO fcm_tokens (user_id, fcm_token, device_type, device_id, localite, is_active, last_used)
    VALUES (p_user_id, p_fcm_token, p_device_type, p_device_id, p_localite, true, NOW())
    ON CONFLICT (fcm_token) 
    DO UPDATE SET 
        user_id = EXCLUDED.user_id,
        device_type = EXCLUDED.device_type,
        device_id = EXCLUDED.device_id,
        localite = EXCLUDED.localite,
        is_active = true,
        updated_at = NOW(),
        last_used = NOW()
    RETURNING id INTO token_id;
    
    result := json_build_object(
        'success', true,
        'action', 'upserted',
        'token_id', token_id,
        'message', 'Token FCM enregistré avec succès'
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- 8. Créer la fonction pour désactiver un token
CREATE OR REPLACE FUNCTION deactivate_fcm_token_simple(p_fcm_token TEXT)
RETURNS JSON AS $$
DECLARE
    result JSON;
    affected_rows INTEGER;
BEGIN
    UPDATE fcm_tokens 
    SET is_active = false, updated_at = NOW()
    WHERE fcm_token = p_fcm_token;
    
    GET DIAGNOSTICS affected_rows = ROW_COUNT;
    
    IF affected_rows > 0 THEN
        result := json_build_object(
            'success', true,
            'message', 'Token FCM désactivé avec succès'
        );
    ELSE
        result := json_build_object(
            'success', false,
            'message', 'Token FCM non trouvé'
        );
    END IF;
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- 9. Créer la fonction pour mettre à jour la dernière utilisation
CREATE OR REPLACE FUNCTION update_fcm_token_last_used_simple(p_fcm_token TEXT)
RETURNS JSON AS $$
DECLARE
    result JSON;
    affected_rows INTEGER;
BEGIN
    UPDATE fcm_tokens 
    SET last_used = NOW(), updated_at = NOW()
    WHERE fcm_token = p_fcm_token AND is_active = true;
    
    GET DIAGNOSTICS affected_rows = ROW_COUNT;
    
    IF affected_rows > 0 THEN
        result := json_build_object(
            'success', true,
            'message', 'Dernière utilisation mise à jour'
        );
    ELSE
        result := json_build_object(
            'success', false,
            'message', 'Token FCM non trouvé ou inactif'
        );
    END IF;
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- 10. Vérifier que tout a été créé
SELECT 'Table fcm_tokens créée' as status, COUNT(*) as count FROM fcm_tokens;

SELECT 'Fonctions créées' as status, routine_name 
FROM information_schema.routines 
WHERE routine_name LIKE '%fcm%'
ORDER BY routine_name;

-- 11. Test de la fonction (optionnel - décommentez pour tester)
/*
SELECT upsert_fcm_token_simple(3, 'test_token_123', 'android', 'test_device', 'Mali');
*/
