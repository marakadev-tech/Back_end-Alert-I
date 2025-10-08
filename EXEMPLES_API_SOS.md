# 🧪 Exemples d'utilisation API Signalements SOS

## 📋 Tests avec cURL

### 1. Créer un signalement anonyme d'inondation

```bash
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{
    "typeUrgence": "inondation",
    "description": "Rue principale inondée après la pluie, circulation impossible. Niveau d'eau atteint 50cm.",
    "localite": "Dakar",
    "latitude": 14.6928,
    "longitude": -17.4467,
    "priorite": "haute"
  }'
```

**Réponse attendue :**
```json
{
  "success": true,
  "message": "Signalement anonyme créé avec succès",
  "data": {
    "id": 1,
    "userId": 0,
    "typeUrgence": "inondation",
    "description": "Rue principale inondée après la pluie, circulation impossible. Niveau d'eau atteint 50cm.",
    "localite": "Dakar",
    "latitude": 14.6928,
    "longitude": -17.4467,
    "statut": "en_cours",
    "priorite": "haute",
    "anonyme": true,
    "timestamp": "2025-01-27T10:30:00.000Z"
  }
}
```

### 2. Créer un signalement critique d'accident

```bash
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{
    "typeUrgence": "accident",
    "description": "Accident de voiture grave au carrefour, plusieurs blessés sur place. Ambulance nécessaire.",
    "localite": "Thiès",
    "latitude": 14.7886,
    "longitude": -16.9260,
    "priorite": "critique",
    "numeroUrgence": "+221771234567"
  }'
```

### 3. Créer un signalement avec photo

```bash
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{
    "typeUrgence": "infrastructure",
    "description": "Panneau de signalisation tombé sur la route, danger pour la circulation",
    "localite": "Saint-Louis",
    "latitude": 16.0320,
    "longitude": -16.4902,
    "priorite": "moyenne",
    "photoUrl": "https://example.com/panneau-tombe.jpg"
  }'
```

### 4. Créer un signalement authentifié

```bash
curl -X POST "http://localhost:8086/api/sos/signal" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 123,
    "typeUrgence": "incendie",
    "description": "Début d'incendie dans un bâtiment commercial",
    "localite": "Kaolack",
    "latitude": 14.1652,
    "longitude": -16.0758,
    "priorite": "critique",
    "anonyme": false
  }'
```

## 📥 Consultation des signalements

### 5. Obtenir tous les signalements

```bash
curl -X GET "http://localhost:8086/api/sos/signaux"
```

### 6. Obtenir les signalements d'un utilisateur

```bash
curl -X GET "http://localhost:8086/api/sos/signaux/user/123"
```

### 7. Obtenir un signalement par ID

```bash
curl -X GET "http://localhost:8086/api/sos/signal/1"
```

### 8. Obtenir les signalements urgents

```bash
curl -X GET "http://localhost:8086/api/sos/urgents"
```

## 🔧 Gestion administrative

### 9. Mettre à jour le statut d'un signalement

```bash
curl -X PATCH "http://localhost:8086/api/sos/signal/1/statut" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -d '{"statut": "traite"}'
```

**Réponse :**
```json
{
  "success": true,
  "message": "Statut mis à jour avec succès"
}
```

### 10. Marquer un signalement comme résolu

```bash
curl -X PATCH "http://localhost:8086/api/sos/signal/1/statut" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -d '{"statut": "resolu"}'
```

## 📊 Statistiques et métadonnées

### 11. Obtenir les statistiques

```bash
curl -X GET "http://localhost:8086/api/sos/statistiques"
```

**Réponse :**
```json
{
  "success": true,
  "data": {
    "total": 15,
    "par_statut": {
      "en_cours": 8,
      "traite": 5,
      "resolu": 2
    },
    "par_priorite": {
      "faible": 3,
      "moyenne": 6,
      "haute": 4,
      "critique": 2
    },
    "par_type": {
      "inondation": 6,
      "accident": 3,
      "infrastructure": 4,
      "incendie": 2
    }
  }
}
```

### 12. Obtenir les types d'urgence disponibles

```bash
curl -X GET "http://localhost:8086/api/sos/types-urgence"
```

### 13. Obtenir les priorités disponibles

```bash
curl -X GET "http://localhost:8086/api/sos/priorites"
```

### 14. Obtenir les statuts disponibles

```bash
curl -X GET "http://localhost:8086/api/sos/statuts"
```

## 🧪 Scénarios de test complets

### Scénario 1 : Signalement d'inondation avec suivi

1. **Créer le signalement**
```bash
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{
    "typeUrgence": "inondation",
    "description": "Rue inondée niveau critique",
    "localite": "Dakar",
    "latitude": 14.6928,
    "longitude": -17.4467,
    "priorite": "critique"
  }'
```

2. **Vérifier le signalement créé**
```bash
curl -X GET "http://localhost:8086/api/sos/signal/1"
```

3. **Mettre à jour le statut**
```bash
curl -X PATCH "http://localhost:8086/api/sos/signal/1/statut" \
  -H "Content-Type: application/json" \
  -d '{"statut": "traite"}'
```

4. **Marquer comme résolu**
```bash
curl -X PATCH "http://localhost:8086/api/sos/signal/1/statut" \
  -H "Content-Type: application/json" \
  -d '{"statut": "resolu"}'
```

### Scénario 2 : Test des différents types d'urgence

```bash
# Inondation
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{"typeUrgence": "inondation", "description": "Test inondation", "localite": "Dakar", "latitude": 14.6928, "longitude": -17.4467, "priorite": "moyenne"}'

# Accident
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{"typeUrgence": "accident", "description": "Test accident", "localite": "Thiès", "latitude": 14.7886, "longitude": -16.9260, "priorite": "haute"}'

# Incendie
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{"typeUrgence": "incendie", "description": "Test incendie", "localite": "Saint-Louis", "latitude": 16.0320, "longitude": -16.4902, "priorite": "critique"}'

# Infrastructure
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{"typeUrgence": "infrastructure", "description": "Test infrastructure", "localite": "Kaolack", "latitude": 14.1652, "longitude": -16.0758, "priorite": "faible"}'
```

### Scénario 3 : Test des priorités

```bash
# Priorité faible
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{"typeUrgence": "infrastructure", "description": "Panneau tombé", "localite": "Dakar", "latitude": 14.6928, "longitude": -17.4467, "priorite": "faible"}'

# Priorité moyenne
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{"typeUrgence": "infrastructure", "description": "Nid de poule dangereux", "localite": "Dakar", "latitude": 14.6928, "longitude": -17.4467, "priorite": "moyenne"}'

# Priorité haute
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{"typeUrgence": "inondation", "description": "Rue inondée", "localite": "Dakar", "latitude": 14.6928, "longitude": -17.4467, "priorite": "haute"}'

# Priorité critique
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{"typeUrgence": "accident", "description": "Accident grave avec blessés", "localite": "Dakar", "latitude": 14.6928, "longitude": -17.4467, "priorite": "critique"}'
```

## 🐛 Tests d'erreur

### Test avec données manquantes

```bash
# Test sans coordonnées GPS (doit échouer)
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{
    "typeUrgence": "inondation",
    "description": "Test sans coordonnées",
    "localite": "Dakar"
  }'
```

### Test avec type d'urgence invalide

```bash
# Test avec type d'urgence non supporté
curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
  -H "Content-Type: application/json" \
  -d '{
    "typeUrgence": "type_invalide",
    "description": "Test type invalide",
    "localite": "Dakar",
    "latitude": 14.6928,
    "longitude": -17.4467,
    "priorite": "moyenne"
  }'
```

### Test de signalement inexistant

```bash
# Test récupération signalement inexistant
curl -X GET "http://localhost:8086/api/sos/signal/99999"
```

## 📱 Tests avec Postman

### Collection Postman

Créez une collection Postman avec les requêtes suivantes :

1. **Create Anonymous SOS Signal**
   - Method: POST
   - URL: `http://localhost:8086/api/sos/signal-anonyme`
   - Body: JSON (voir exemples ci-dessus)

2. **Get All SOS Signals**
   - Method: GET
   - URL: `http://localhost:8086/api/sos/signaux`

3. **Get SOS Signal by ID**
   - Method: GET
   - URL: `http://localhost:8086/api/sos/signal/{{signalId}}`

4. **Update SOS Signal Status**
   - Method: PATCH
   - URL: `http://localhost:8086/api/sos/signal/{{signalId}}/statut`
   - Body: `{"statut": "traite"}`

5. **Get SOS Statistics**
   - Method: GET
   - URL: `http://localhost:8086/api/sos/statistiques`

### Variables Postman

- `baseUrl`: `http://localhost:8086`
- `signalId`: `1` (ou l'ID du dernier signalement créé)

## 🔍 Validation des résultats

### Checklist de validation

Après chaque test, vérifiez :

- [ ] Le signalement est créé avec un ID unique
- [ ] Les coordonnées GPS sont correctement stockées
- [ ] Le statut par défaut est "en_cours"
- [ ] La priorité est respectée
- [ ] Le timestamp est automatiquement généré
- [ ] Les notifications sont envoyées (vérifier les logs)
- [ ] Les statistiques sont mises à jour
- [ ] Les signalements anonymes sont autorisés
- [ ] Les signalements critiques déclenchent des alertes spéciales

### Logs à surveiller

```
✅ Signalement SOS créé avec succès pour l'utilisateur 0
📱 Notification de confirmation SOS envoyée à l'utilisateur 0
📢 Notification SOS générale envoyée à tous les utilisateurs
🚨 ALERTE CRITIQUE - Notification d'urgence critique envoyée!
```

## 🚀 Tests de performance

### Test de charge simple

```bash
# Créer plusieurs signalements rapidement
for i in {1..10}; do
  curl -X POST "http://localhost:8086/api/sos/signal-anonyme" \
    -H "Content-Type: application/json" \
    -d "{\"typeUrgence\": \"inondation\", \"description\": \"Test $i\", \"localite\": \"Dakar\", \"latitude\": 14.6928, \"longitude\": -17.4467, \"priorite\": \"moyenne\"}" &
done
wait
```

### Test de récupération

```bash
# Récupérer tous les signalements
time curl -X GET "http://localhost:8086/api/sos/signaux"
```

---

**Ces exemples vous permettront de tester complètement le système de signalements SOS !** 🎯
