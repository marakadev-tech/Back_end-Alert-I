-- ============================================================
-- Seed Quiz Data: Environnement / Climat / Inondations
-- Compatible with tables: quiz, questions, reponses
-- Safe to re-run (checks existing records before insert)
-- ============================================================

DO $$
DECLARE
  v_quiz_id INTEGER;
  v_question_id INTEGER;
BEGIN
  -- ----------------------------------------------------------
  -- QUIZ 1: ENVIRONNEMENT
  -- ----------------------------------------------------------
  SELECT id INTO v_quiz_id
  FROM public.quiz
  WHERE description = 'Quiz - Environnement'
  LIMIT 1;

  IF v_quiz_id IS NULL THEN
    INSERT INTO public.quiz (description)
    VALUES ('Quiz - Environnement')
    RETURNING id INTO v_quiz_id;
  END IF;

  -- Q1
  SELECT id INTO v_question_id FROM public.questions
  WHERE quiz_id = v_quiz_id AND textequestion = 'Quel geste réduit le plus les déchets plastiques au quotidien ?'
  LIMIT 1;
  IF v_question_id IS NULL THEN
    INSERT INTO public.questions (quiz_id, textequestion)
    VALUES (v_quiz_id, 'Quel geste réduit le plus les déchets plastiques au quotidien ?')
    RETURNING id INTO v_question_id;
    INSERT INTO public.reponses (question_id, reponse1, reponse2, reponse3, "estCorrect")
    VALUES (v_question_id, 'Utiliser une gourde réutilisable', 'Bruler les bouteilles plastiques', 'Jeter les déchets dans les caniveaux', 'reponse1');
  END IF;

  -- Q2
  SELECT id INTO v_question_id FROM public.questions
  WHERE quiz_id = v_quiz_id AND textequestion = 'Pourquoi le tri des déchets est-il important ?'
  LIMIT 1;
  IF v_question_id IS NULL THEN
    INSERT INTO public.questions (quiz_id, textequestion)
    VALUES (v_quiz_id, 'Pourquoi le tri des déchets est-il important ?')
    RETURNING id INTO v_question_id;
    INSERT INTO public.reponses (question_id, reponse1, reponse2, reponse3, "estCorrect")
    VALUES (v_question_id, 'Il permet de recycler et de réduire la pollution', 'Il augmente le volume des déchets', 'Il n''a aucun effet', 'reponse1');
  END IF;

  -- Q3
  SELECT id INTO v_question_id FROM public.questions
  WHERE quiz_id = v_quiz_id AND textequestion = 'Quelle action aide à préserver l''eau potable ?'
  LIMIT 1;
  IF v_question_id IS NULL THEN
    INSERT INTO public.questions (quiz_id, textequestion)
    VALUES (v_quiz_id, 'Quelle action aide à préserver l''eau potable ?')
    RETURNING id INTO v_question_id;
    INSERT INTO public.reponses (question_id, reponse1, reponse2, reponse3, "estCorrect")
    VALUES (v_question_id, 'Réparer les fuites rapidement', 'Laisser le robinet couler inutilement', 'Laver les rues avec de l''eau potable chaque jour', 'reponse1');
  END IF;

  -- ----------------------------------------------------------
  -- QUIZ 2: CHANGEMENT CLIMATIQUE
  -- ----------------------------------------------------------
  SELECT id INTO v_quiz_id
  FROM public.quiz
  WHERE description = 'Quiz - Changement climatique'
  LIMIT 1;

  IF v_quiz_id IS NULL THEN
    INSERT INTO public.quiz (description)
    VALUES ('Quiz - Changement climatique')
    RETURNING id INTO v_quiz_id;
  END IF;

  -- Q1
  SELECT id INTO v_question_id FROM public.questions
  WHERE quiz_id = v_quiz_id AND textequestion = 'Quel gaz contribue fortement au réchauffement climatique ?'
  LIMIT 1;
  IF v_question_id IS NULL THEN
    INSERT INTO public.questions (quiz_id, textequestion)
    VALUES (v_quiz_id, 'Quel gaz contribue fortement au réchauffement climatique ?')
    RETURNING id INTO v_question_id;
    INSERT INTO public.reponses (question_id, reponse1, reponse2, reponse3, "estCorrect")
    VALUES (v_question_id, 'Le dioxyde de carbone (CO2)', 'L''oxygène (O2)', 'L''azote (N2)', 'reponse1');
  END IF;

  -- Q2
  SELECT id INTO v_question_id FROM public.questions
  WHERE quiz_id = v_quiz_id AND textequestion = 'Quelle conséquence est liée au changement climatique ?'
  LIMIT 1;
  IF v_question_id IS NULL THEN
    INSERT INTO public.questions (quiz_id, textequestion)
    VALUES (v_quiz_id, 'Quelle conséquence est liée au changement climatique ?')
    RETURNING id INTO v_question_id;
    INSERT INTO public.reponses (question_id, reponse1, reponse2, reponse3, "estCorrect")
    VALUES (v_question_id, 'Des événements météo extrêmes plus fréquents', 'La disparition du cycle des saisons en une semaine', 'La fin des pluies pour toujours', 'reponse1');
  END IF;

  -- Q3
  SELECT id INTO v_question_id FROM public.questions
  WHERE quiz_id = v_quiz_id AND textequestion = 'Quel comportement réduit les émissions de gaz à effet de serre ?'
  LIMIT 1;
  IF v_question_id IS NULL THEN
    INSERT INTO public.questions (quiz_id, textequestion)
    VALUES (v_quiz_id, 'Quel comportement réduit les émissions de gaz à effet de serre ?')
    RETURNING id INTO v_question_id;
    INSERT INTO public.reponses (question_id, reponse1, reponse2, reponse3, "estCorrect")
    VALUES (v_question_id, 'Privilégier les transports en commun', 'Bruler les déchets ménagers', 'Laisser les appareils allumés en permanence', 'reponse1');
  END IF;

  -- ----------------------------------------------------------
  -- QUIZ 3: INONDATIONS
  -- ----------------------------------------------------------
  SELECT id INTO v_quiz_id
  FROM public.quiz
  WHERE description = 'Quiz - Prévention des inondations'
  LIMIT 1;

  IF v_quiz_id IS NULL THEN
    INSERT INTO public.quiz (description)
    VALUES ('Quiz - Prévention des inondations')
    RETURNING id INTO v_quiz_id;
  END IF;

  -- Q1
  SELECT id INTO v_question_id FROM public.questions
  WHERE quiz_id = v_quiz_id AND textequestion = 'Quelle est la première action en cas d''alerte inondation ?'
  LIMIT 1;
  IF v_question_id IS NULL THEN
    INSERT INTO public.questions (quiz_id, textequestion)
    VALUES (v_quiz_id, 'Quelle est la première action en cas d''alerte inondation ?')
    RETURNING id INTO v_question_id;
    INSERT INTO public.reponses (question_id, reponse1, reponse2, reponse3, "estCorrect")
    VALUES (v_question_id, 'Se mettre en sécurité et suivre les consignes officielles', 'Descendre au sous-sol pour proteger les biens', 'Traverser les zones déjà inondées à pied', 'reponse1');
  END IF;

  -- Q2
  SELECT id INTO v_question_id FROM public.questions
  WHERE quiz_id = v_quiz_id AND textequestion = 'Pourquoi ne faut-il pas traverser une route inondée ?'
  LIMIT 1;
  IF v_question_id IS NULL THEN
    INSERT INTO public.questions (quiz_id, textequestion)
    VALUES (v_quiz_id, 'Pourquoi ne faut-il pas traverser une route inondée ?')
    RETURNING id INTO v_question_id;
    INSERT INTO public.reponses (question_id, reponse1, reponse2, reponse3, "estCorrect")
    VALUES (v_question_id, 'Le courant peut emporter une personne ou un véhicule', 'L''eau rend la route plus propre', 'Cela réduit le risque d''accident', 'reponse1');
  END IF;

  -- Q3
  SELECT id INTO v_question_id FROM public.questions
  WHERE quiz_id = v_quiz_id AND textequestion = 'Quel aménagement aide à limiter les inondations en ville ?'
  LIMIT 1;
  IF v_question_id IS NULL THEN
    INSERT INTO public.questions (quiz_id, textequestion)
    VALUES (v_quiz_id, 'Quel aménagement aide à limiter les inondations en ville ?')
    RETURNING id INTO v_question_id;
    INSERT INTO public.reponses (question_id, reponse1, reponse2, reponse3, "estCorrect")
    VALUES (v_question_id, 'Entretenir les caniveaux et les systèmes de drainage', 'Boucher les caniveaux avec des déchets', 'Construire dans les zones inondables sans étude', 'reponse1');
  END IF;

END $$;

-- Quick check
SELECT q.id, q.description, COUNT(qu.id) AS questions_count
FROM public.quiz q
LEFT JOIN public.questions qu ON qu.quiz_id = q.id
WHERE q.description IN (
  'Quiz - Environnement',
  'Quiz - Changement climatique',
  'Quiz - Prévention des inondations'
)
GROUP BY q.id, q.description
ORDER BY q.id;
