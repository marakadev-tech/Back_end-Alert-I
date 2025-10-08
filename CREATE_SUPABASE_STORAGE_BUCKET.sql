-- Script pour créer le bucket de stockage Supabase pour les images SOS
-- À exécuter dans la console Supabase SQL Editor

-- 1. Créer le bucket pour les images SOS
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'sos-images',
  'sos-images',
  true,  -- Bucket public pour accès direct aux images
  52428800,  -- Limite de 50MB par fichier
  ARRAY['image/jpeg', 'image/png', 'image/webp']  -- Types d'images autorisés
);

-- 2. Créer une politique RLS pour permettre l'upload (authentifié)
CREATE POLICY "Allow authenticated users to upload SOS images" ON storage.objects
FOR INSERT 
TO authenticated
WITH CHECK (bucket_id = 'sos-images');

-- 3. Créer une politique RLS pour permettre la lecture (public)
CREATE POLICY "Allow public read access to SOS images" ON storage.objects
FOR SELECT 
TO public
USING (bucket_id = 'sos-images');

-- 4. Créer une politique RLS pour permettre la suppression (authentifié)
CREATE POLICY "Allow authenticated users to delete SOS images" ON storage.objects
FOR DELETE 
TO authenticated
USING (bucket_id = 'sos-images');

-- 5. Vérifier que le bucket a été créé
SELECT * FROM storage.buckets WHERE id = 'sos-images';

-- 6. Vérifier les politiques RLS
SELECT * FROM pg_policies WHERE tablename = 'objects' AND schemaname = 'storage';
