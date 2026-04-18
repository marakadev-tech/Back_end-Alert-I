-- ============================================================
-- Seed Rewards Data: Contextualized for Mali
-- Compatible with table: rewards
-- Safe to re-run (without requiring UNIQUE on name)
-- ============================================================

-- 1) Stage seed data
CREATE TEMP TABLE tmp_rewards_seed (
  name TEXT,
  description TEXT,
  points_required INTEGER,
  category TEXT,
  image_url TEXT,
  is_active BOOLEAN,
  stock INTEGER
) ON COMMIT DROP;

INSERT INTO tmp_rewards_seed (
  name, description, points_required, category, image_url, is_active, stock
)
VALUES
  ('Bon d''eau potable (20L)', 'Bon échangeable contre de l''eau potable auprès de points partenaires.', 300, 'Eau', 'assets/images/rewards/eau_20l.png', TRUE, 300),
  ('Kit hygiène familiale', 'Kit comprenant savon, gel, javel et produits d''hygiène de base.', 700, 'Santé', 'assets/images/rewards/kit_hygiene.png', TRUE, 150),
  ('Réduction transport urbain', 'Réduction sur un abonnement ou des tickets de transport local.', 500, 'Transport', 'assets/images/rewards/transport_reduction.png', TRUE, 200),
  ('Recharge téléphonique (1 000 FCFA)', 'Crédit téléphonique pour rester joignable en période de crise.', 400, 'Communication', 'assets/images/rewards/recharge_1000.png', TRUE, 400),
  ('Pack scolaire (cahiers + stylos)', 'Fournitures scolaires de base pour les enfants affectés.', 800, 'Education', 'assets/images/rewards/pack_scolaire.png', TRUE, 120),
  ('Lampe solaire portable', 'Lampe solaire rechargeable utile en cas de coupure électrique.', 1200, 'Énergie', 'assets/images/rewards/lampe_solaire.png', TRUE, 80),
  ('Kit d''urgence inondation', 'Kit de base avec gants, sacs étanches, sifflet et couverture.', 1500, 'Sécurité', 'assets/images/rewards/kit_urgence_inondation.png', TRUE, 60),
  ('Réduction facture d''eau', 'Bon de réduction appliqué sur facture d''eau auprès de partenaires.', 1000, 'Eau', 'assets/images/rewards/reduction_eau.png', TRUE, 100),
  ('Réduction facture électricité', 'Bon de réduction énergie pour foyers en zone à risque.', 1300, 'Énergie', 'assets/images/rewards/reduction_electricite.png', TRUE, 100),
  ('Consultation santé gratuite', 'Consultation de base auprès d''un centre de santé partenaire.', 900, 'Santé', 'assets/images/rewards/consultation_sante.png', TRUE, 90);

-- 2) Update existing rewards by name
UPDATE public.rewards r
SET
  description = s.description,
  points_required = s.points_required,
  category = s.category,
  image_url = s.image_url,
  is_active = s.is_active,
  stock = s.stock,
  updated_at = NOW()
FROM tmp_rewards_seed s
WHERE r.name = s.name;

-- 3) Insert missing rewards
INSERT INTO public.rewards (
  name, description, points_required, category, image_url, is_active, stock
)
SELECT
  s.name, s.description, s.points_required, s.category, s.image_url, s.is_active, s.stock
FROM tmp_rewards_seed s
WHERE NOT EXISTS (
  SELECT 1 FROM public.rewards r WHERE r.name = s.name
);

-- Quick check
SELECT id, name, points_required, category, stock, is_active
FROM public.rewards
ORDER BY points_required ASC, id ASC;
