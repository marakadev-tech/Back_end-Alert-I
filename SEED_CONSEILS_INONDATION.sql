-- ============================================================
-- Seed Conseils Data: Inondation (Mali)
-- Compatible with table: conseil (type, description)
-- Safe to re-run (no duplicate insert on same type+description)
-- ============================================================

-- 1) Stage seed data
CREATE TEMP TABLE tmp_conseils_seed (
  type TEXT,
  description TEXT
) ON COMMIT DROP;

INSERT INTO tmp_conseils_seed (type, description)
VALUES
  -- AVANT INONDATION
  ('Avant inondation', 'Suivez les alertes météo locales et identifiez les zones inondables proches de votre domicile.'),
  ('Avant inondation', 'Préparez un kit d''urgence: eau potable, lampe, radio, médicaments, documents importants protégés dans un sac étanche.'),
  ('Avant inondation', 'Surélevez les objets de valeur et les appareils électriques dans la maison.'),
  ('Avant inondation', 'Nettoyez caniveaux, drains et alentours pour faciliter l''écoulement de l''eau.'),
  ('Avant inondation', 'Repérez un itinéraire d''évacuation vers un lieu sûr en hauteur et partagez-le avec la famille.'),

  -- PENDANT INONDATION
  ('Pendant inondation', 'Évitez de traverser l''eau à pied ou en véhicule, même si elle semble peu profonde.'),
  ('Pendant inondation', 'Coupez l''électricité au disjoncteur si cela peut être fait sans danger.'),
  ('Pendant inondation', 'Éloignez-vous des berges, ponts fragiles, fils électriques tombés et murs instables.'),
  ('Pendant inondation', 'Montez vers une zone plus élevée et restez à l''écoute des consignes officielles.'),
  ('Pendant inondation', 'Utilisez l''appel d''urgence uniquement en cas de danger immédiat pour ne pas saturer les secours.'),

  -- APRES INONDATION
  ('Apres inondation', 'Ne rentrez pas chez vous avant autorisation des autorités locales.'),
  ('Apres inondation', 'Portez des gants et des bottes pour le nettoyage afin d''éviter les infections.'),
  ('Apres inondation', 'N''utilisez pas l''eau du robinet sans vérification; privilégiez l''eau traitée ou bouillie.'),
  ('Apres inondation', 'Faites vérifier l''installation électrique avant de remettre le courant.'),
  ('Apres inondation', 'Photographiez les dégâts et conservez les preuves pour toute assistance ou déclaration.');

-- 2) Insert only missing conseils
INSERT INTO public.conseil (type, description)
SELECT s.type, s.description
FROM tmp_conseils_seed s
WHERE NOT EXISTS (
  SELECT 1
  FROM public.conseil c
  WHERE c.type = s.type
    AND c.description = s.description
);

-- Quick check
SELECT type, COUNT(*) AS total
FROM public.conseil
WHERE type IN ('Avant inondation', 'Pendant inondation', 'Apres inondation')
GROUP BY type
ORDER BY type;
