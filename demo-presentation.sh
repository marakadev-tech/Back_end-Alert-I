#!/bin/bash

# Script de démonstration pour la présentation
# Usage: ./demo-presentation.sh [URL_RAILWAY]
# Exemple: ./demo-presentation.sh https://mon-app.up.railway.app

echo "🎤 SCRIPT DE DÉMONSTRATION - SYSTÈME D'ALERTES"
echo "=============================================="
echo ""

# Configuration
# Vous pouvez passer l'URL en paramètre ou l'exporter comme variable d'environnement
if [ ! -z "$1" ]; then
    RAILWAY_URL="$1"
    echo "📝 URL Railway fournie en paramètre: $RAILWAY_URL"
elif [ ! -z "$RAILWAY_URL" ]; then
    echo "📝 URL Railway depuis variable d'environnement: $RAILWAY_URL"
else
    RAILWAY_URL="https://votre-app.up.railway.app"
    echo "⚠️  URL Railway par défaut (remplacez par la vraie URL): $RAILWAY_URL"
    echo "   Usage: ./demo-presentation.sh https://mon-app.up.railway.app"
    echo "   Ou exportez: export RAILWAY_URL=https://mon-app.up.railway.app"
    echo ""
fi

echo "🌐 URL du backend Railway: $RAILWAY_URL"
echo ""

# Fonction pour exécuter une commande curl
run_curl() {
    local endpoint=$1
    local description=$2
    echo "🔄 $description"
    echo "   Endpoint: $endpoint"
    curl -s -X GET "$RAILWAY_URL$endpoint" | jq '.' 2>/dev/null || echo "   Réponse reçue (format non-JSON)"
    echo ""
}

# Fonction pour attendre
wait_demo() {
    local seconds=$1
    echo "⏳ Attente de $seconds secondes..."
    sleep $seconds
    echo ""
}

echo "🚀 DÉMARRAGE DE LA DÉMONSTRATION"
echo "================================"
echo ""

# 1. Vérifier le statut initial
echo "📊 1. VÉRIFICATION DU STATUT INITIAL"
run_curl "/api/demo/status" "Statut du mode démonstration"

# 2. Activer le mode démonstration
echo "🎯 2. ACTIVATION DU MODE DÉMONSTRATION"
run_curl "/api/demo/enable" "Activation du mode démonstration automatique"

# 3. Vérifier les statistiques FCM
echo "📱 3. VÉRIFICATION DES TOKENS FCM"
run_curl "/api/notifications/stats" "Statistiques des tokens FCM"

# 4. Déclencher une démonstration immédiate
echo "⚡ 4. DÉMONSTRATION IMMÉDIATE"
run_curl "/api/demo/trigger-now" "Déclenchement immédiat d'une notification"

# 5. Attendre et montrer les notifications automatiques
echo "⏰ 5. NOTIFICATIONS AUTOMATIQUES"
echo "   Le système va maintenant envoyer des notifications automatiquement :"
echo "   - Toutes les 30 secondes : Démonstration générale"
echo "   - Toutes les 45 secondes : Démonstration avec capteur spécifique"
echo ""

# Attendre 2 minutes pour montrer les notifications automatiques
echo "🎬 DÉMONSTRATION EN COURS - 2 minutes"
for i in {1..4}; do
    wait_demo 30
    echo "📊 Vérification du statut (démonstration #$i)"
    run_curl "/api/demo/status" "Statut actuel"
done

# 6. Désactiver le mode démonstration
echo "🛑 6. DÉSACTIVATION DU MODE DÉMONSTRATION"
run_curl "/api/demo/disable" "Désactivation du mode démonstration"

# 7. Test final
echo "✅ 7. TEST FINAL"
run_curl "/api/weather/test-push-simple" "Test final de notification"

echo "🎉 DÉMONSTRATION TERMINÉE"
echo "========================"
echo ""
echo "📋 RÉSUMÉ DE LA DÉMONSTRATION :"
echo "   ✅ Mode démonstration activé/désactivé"
echo "   ✅ Notifications automatiques toutes les 30 secondes"
echo "   ✅ Notifications avec capteur spécifique toutes les 45 secondes"
echo "   ✅ Système entièrement automatisé"
echo ""
echo "🎯 Le système est prêt pour la production !"
