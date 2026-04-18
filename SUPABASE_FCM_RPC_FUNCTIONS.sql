-- ============================================================
-- RPC FCM pour Flutter (PostgREST: /rest/v1/rpc/...)
-- À exécuter sur un projet Supabase qui a déjà public.fcm_tokens
-- mais où ces fonctions manquent (erreur PGRST202 / 404).
-- Idempotent: CREATE OR REPLACE
-- ============================================================

CREATE OR REPLACE FUNCTION public.upsert_fcm_token_simple(
  p_user_id INTEGER,
  p_fcm_token TEXT,
  p_device_type TEXT DEFAULT 'android',
  p_device_id TEXT DEFAULT NULL,
  p_localite TEXT DEFAULT 'Mali'
)
RETURNS JSON
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  result JSON;
  token_id UUID;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM public.users u WHERE u.id = p_user_id) THEN
    RETURN json_build_object(
      'success', false,
      'message', 'Utilisateur non trouvé'
    );
  END IF;

  INSERT INTO public.fcm_tokens (
    user_id, fcm_token, device_type, device_id, localite, is_active, last_used
  )
  VALUES (
    p_user_id, p_fcm_token, p_device_type, p_device_id, p_localite, true, NOW()
  )
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
$$;

CREATE OR REPLACE FUNCTION public.deactivate_fcm_token_simple(p_fcm_token TEXT)
RETURNS JSON
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  result JSON;
  affected_rows INTEGER;
BEGIN
  UPDATE public.fcm_tokens
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
$$;

CREATE OR REPLACE FUNCTION public.update_fcm_token_last_used_simple(p_fcm_token TEXT)
RETURNS JSON
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  result JSON;
  affected_rows INTEGER;
BEGIN
  UPDATE public.fcm_tokens
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
$$;

GRANT EXECUTE ON FUNCTION public.upsert_fcm_token_simple(
  INTEGER, TEXT, TEXT, TEXT, TEXT
) TO anon, authenticated, service_role;

GRANT EXECUTE ON FUNCTION public.deactivate_fcm_token_simple(TEXT)
  TO anon, authenticated, service_role;

GRANT EXECUTE ON FUNCTION public.update_fcm_token_last_used_simple(TEXT)
  TO anon, authenticated, service_role;
