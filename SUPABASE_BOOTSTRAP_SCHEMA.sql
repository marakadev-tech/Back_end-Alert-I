-- ============================================================
-- Alert'I - Supabase Bootstrap Schema
-- Recreates the main tables expected by the Spring backend.
-- Execute in Supabase SQL editor on a fresh project.
-- ============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS postgis;

-- ------------------------------------------------------------
-- Generic trigger function for updated_at
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ------------------------------------------------------------
-- USERS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.users (
  id SERIAL PRIMARY KEY,
  prenom TEXT,
  nom TEXT,
  email TEXT UNIQUE,
  password TEXT NOT NULL,
  role TEXT DEFAULT 'Citoyen',
  localite TEXT,
  num_tel INTEGER UNIQUE NOT NULL,
  point INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- If users table already existed before this bootstrap, ensure points column exists.
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS point INTEGER NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_num_tel ON public.users(num_tel);
CREATE INDEX IF NOT EXISTS idx_users_point ON public.users(point);

DROP TRIGGER IF EXISTS trg_users_updated_at ON public.users;
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON public.users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- CAPTEURS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.sensors (
  id TEXT PRIMARY KEY,
  statut TEXT DEFAULT 'active',
  localite TEXT,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  "dernierDonneeCaptemperature" DOUBLE PRECISION,
  "dernierDonneeCapniveauEau" DOUBLE PRECISION,
  "dernierDonneevitesseDuVent" DOUBLE PRECISION,
  "seuilniveauEau" DOUBLE PRECISION,
  "pluviometrieJour" DOUBLE PRECISION,
  "seuilPluviometrie" DOUBLE PRECISION,
  "timestamp" TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sensors_statut ON public.sensors(statut);
CREATE INDEX IF NOT EXISTS idx_sensors_localite ON public.sensors(localite);

DROP TRIGGER IF EXISTS trg_sensors_updated_at ON public.sensors;
CREATE TRIGGER trg_sensors_updated_at
BEFORE UPDATE ON public.sensors
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Historique des mesures capteurs
CREATE TABLE IF NOT EXISTS public.history (
  id BIGSERIAL PRIMARY KEY,
  sensor_id TEXT NOT NULL REFERENCES public.sensors(id) ON DELETE CASCADE,
  "timestamp" TIMESTAMPTZ DEFAULT NOW(),
  temperature DOUBLE PRECISION,
  humidity DOUBLE PRECISION,
  "niveauEau" DOUBLE PRECISION,
  "vitesseDuVent" DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_history_sensor_id ON public.history(sensor_id);
CREATE INDEX IF NOT EXISTS idx_history_timestamp ON public.history("timestamp");

-- ------------------------------------------------------------
-- CONSEILS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.conseil (
  id SERIAL PRIMARY KEY,
  type TEXT NOT NULL,
  description TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_conseil_type ON public.conseil(type);

-- ------------------------------------------------------------
-- QUIZ
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.quiz (
  id SERIAL PRIMARY KEY,
  description TEXT NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

DROP TRIGGER IF EXISTS trg_quiz_updated_at ON public.quiz;
CREATE TRIGGER trg_quiz_updated_at
BEFORE UPDATE ON public.quiz
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS public.questions (
  id SERIAL PRIMARY KEY,
  textequestion TEXT NOT NULL,
  quiz_id INTEGER NOT NULL REFERENCES public.quiz(id) ON DELETE CASCADE,
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_questions_quiz_id ON public.questions(quiz_id);

DROP TRIGGER IF EXISTS trg_questions_updated_at ON public.questions;
CREATE TRIGGER trg_questions_updated_at
BEFORE UPDATE ON public.questions
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS public.reponses (
  id SERIAL PRIMARY KEY,
  reponse1 TEXT,
  reponse2 TEXT,
  reponse3 TEXT,
  "estCorrect" TEXT,
  question_id INTEGER NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_reponses_question_id ON public.reponses(question_id);

DROP TRIGGER IF EXISTS trg_reponses_updated_at ON public.reponses;
CREATE TRIGGER trg_reponses_updated_at
BEFORE UPDATE ON public.reponses
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- SOS SIGNALS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.sos_signals (
  id BIGSERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL DEFAULT 0,
  type_urgence TEXT NOT NULL,
  description TEXT NOT NULL,
  localite TEXT NOT NULL,
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  statut TEXT NOT NULL DEFAULT 'en_cours',
  priorite TEXT NOT NULL DEFAULT 'moyenne',
  signal_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  photo_url TEXT,
  numero_urgence TEXT,
  anonyme BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sos_signals_user_id ON public.sos_signals(user_id);
CREATE INDEX IF NOT EXISTS idx_sos_signals_statut ON public.sos_signals(statut);
CREATE INDEX IF NOT EXISTS idx_sos_signals_priorite ON public.sos_signals(priorite);
CREATE INDEX IF NOT EXISTS idx_sos_signals_signal_timestamp ON public.sos_signals(signal_timestamp);

DROP TRIGGER IF EXISTS trg_sos_signals_updated_at ON public.sos_signals;
CREATE TRIGGER trg_sos_signals_updated_at
BEFORE UPDATE ON public.sos_signals
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- FCM TOKENS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.fcm_tokens (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  fcm_token TEXT NOT NULL UNIQUE,
  device_type VARCHAR(50) DEFAULT 'unknown',
  device_id VARCHAR(255),
  localite VARCHAR(255) NOT NULL DEFAULT 'Mali',
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  last_used TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON public.fcm_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_token ON public.fcm_tokens(fcm_token);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_localite ON public.fcm_tokens(localite);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_is_active ON public.fcm_tokens(is_active);

DROP TRIGGER IF EXISTS trg_fcm_tokens_updated_at ON public.fcm_tokens;
CREATE TRIGGER trg_fcm_tokens_updated_at
BEFORE UPDATE ON public.fcm_tokens
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- RPC FCM consommées par Flutter: POST /rest/v1/rpc/<name>
-- SECURITY DEFINER: contourne RLS sur fcm_tokens (appel avec clé anon + apikey)
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

-- ------------------------------------------------------------
-- REWARDS / POINTS CONVERSION
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.rewards (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  points_required INTEGER NOT NULL CHECK (points_required >= 0),
  category TEXT,
  image_url TEXT,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rewards_is_active ON public.rewards(is_active);
CREATE INDEX IF NOT EXISTS idx_rewards_points_required ON public.rewards(points_required);
CREATE INDEX IF NOT EXISTS idx_rewards_category ON public.rewards(category);

DROP TRIGGER IF EXISTS trg_rewards_updated_at ON public.rewards;
CREATE TRIGGER trg_rewards_updated_at
BEFORE UPDATE ON public.rewards
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS public.user_rewards (
  id BIGSERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
  reward_id INTEGER NOT NULL REFERENCES public.rewards(id) ON DELETE CASCADE,
  points_spent INTEGER NOT NULL CHECK (points_spent >= 0),
  status TEXT NOT NULL DEFAULT 'pending',
  claimed_at TIMESTAMPTZ,
  expires_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_rewards_user_id ON public.user_rewards(user_id);
CREATE INDEX IF NOT EXISTS idx_user_rewards_reward_id ON public.user_rewards(reward_id);
CREATE INDEX IF NOT EXISTS idx_user_rewards_created_at ON public.user_rewards(created_at);

DROP TRIGGER IF EXISTS trg_user_rewards_updated_at ON public.user_rewards;
CREATE TRIGGER trg_user_rewards_updated_at
BEFORE UPDATE ON public.user_rewards
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Fonction utilisée par Flutter pour décrémenter le stock
CREATE OR REPLACE FUNCTION public.update_reward_stock_simple(
  reward_id_param INTEGER,
  new_stock_param INTEGER
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
  UPDATE public.rewards
  SET stock = new_stock_param,
      updated_at = NOW()
  WHERE id = reward_id_param
    AND new_stock_param >= 0;

  RETURN FOUND;
END;
$$;

-- Fonction utilisée en fallback pour mise à jour points
CREATE OR REPLACE FUNCTION public.update_user_points(
  user_id_param TEXT,
  new_points INTEGER
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
  UPDATE public.users
  SET point = new_points,
      updated_at = NOW()
  WHERE id::TEXT = user_id_param;

  RETURN FOUND;
END;
$$;

-- Vue leaderboard consommée par Flutter
CREATE OR REPLACE VIEW public.user_leaderboard AS
SELECT
  ROW_NUMBER() OVER (ORDER BY u.point DESC, u.id ASC) AS rank,
  u.id::TEXT AS user_id,
  TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')) AS user_name,
  u.point AS points,
  NULL::TEXT AS avatar_url,
  u.localite
FROM public.users u
WHERE u.point IS NOT NULL;

-- ------------------------------------------------------------
-- Storage bucket (optional, for SOS images)
-- ------------------------------------------------------------
INSERT INTO storage.buckets (id, name, public)
VALUES ('sos-signals', 'sos-signals', TRUE)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- Optional: enable RLS (service_role bypasses RLS anyway)
-- ------------------------------------------------------------
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sensors ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.history ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conseil ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quiz ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.questions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reponses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sos_signals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.fcm_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rewards ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_rewards ENABLE ROW LEVEL SECURITY;

-- Basic read policy examples (adjust for production)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname = 'public' AND tablename = 'sensors' AND policyname = 'public_read_sensors'
  ) THEN
    CREATE POLICY public_read_sensors ON public.sensors FOR SELECT USING (true);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname = 'public' AND tablename = 'sos_signals' AND policyname = 'public_read_sos_signals'
  ) THEN
    CREATE POLICY public_read_sos_signals ON public.sos_signals FOR SELECT USING (true);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname = 'public' AND tablename = 'rewards' AND policyname = 'public_read_rewards'
  ) THEN
    CREATE POLICY public_read_rewards ON public.rewards FOR SELECT USING (true);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname = 'public' AND tablename = 'user_rewards' AND policyname = 'public_read_user_rewards'
  ) THEN
    CREATE POLICY public_read_user_rewards ON public.user_rewards FOR SELECT USING (true);
  END IF;
END $$;

