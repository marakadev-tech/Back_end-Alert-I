#!/bin/bash

# Script de test pour la Lambda de notifications SOS
# Usage: ./test-lambda-notification.sh [YOUR_FCM_TOKEN] [YOUR_API_KEY]

# Couleurs pour les logs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

LAMBDA_URL="https://aywkpqdshe.execute-api.us-east-1.amazonaws.com/default/send_notification"

# Vérifier les arguments
if [ -z "$1" ]; then
    echo -e "${RED}❌ Erreur: Token FCM manquant${NC}"
    echo -e "${YELLOW}Usage: $0 [YOUR_FCM_TOKEN] [YOUR_API_KEY]${NC}"
    echo ""
    echo -e "${BLUE}Pour obtenir votre token FCM:${NC}"
    echo "1. Dans votre app Flutter, ajoutez ce code:"
    echo "   String? token = await FirebaseMessaging.instance.getToken();"
    echo "   print('📱 Token FCM: \$token');"
    echo ""
    exit 1
fi

FCM_TOKEN="$1"
API_KEY="${2:-YOUR_ACTUAL_API_KEY_HERE}"

echo -e "${BLUE}🧪 Test de la Lambda de notifications SOS${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${YELLOW}📍 URL Lambda:${NC} $LAMBDA_URL"
echo -e "${YELLOW}🔑 API Key:${NC} ${API_KEY:0:20}..."
echo -e "${YELLOW}📱 Token FCM:${NC} ${FCM_TOKEN:0:30}..."
echo ""

# Test 1: Notification simple
echo -e "${BLUE}━━━ Test 1: Notification simple ━━━${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$LAMBDA_URL" \
  -H "Content-Type: application/json" \
  -H "x-api-key: $API_KEY" \
  -d "{
    \"token\": \"$FCM_TOKEN\",
    \"title\": \"🧪 Test Lambda\",
    \"body\": \"Test de notification simple\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 207 ]; then
    echo -e "${GREEN}✅ Succès (HTTP $HTTP_CODE)${NC}"
    echo -e "${GREEN}Réponse:${NC}"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}❌ Échec (HTTP $HTTP_CODE)${NC}"
    echo -e "${RED}Réponse:${NC}"
    echo "$BODY"
fi

echo ""
sleep 2

# Test 2: Notification SOS avec données
echo -e "${BLUE}━━━ Test 2: Notification SOS avec données ━━━${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$LAMBDA_URL" \
  -H "Content-Type: application/json" \
  -H "x-api-key: $API_KEY" \
  -d "{
    \"token\": \"$FCM_TOKEN\",
    \"title\": \"🚨 Test SOS\",
    \"body\": \"Signalement: accident à Bamako\",
    \"data\": {
      \"type\": \"sos_signal\",
      \"signalId\": \"999\",
      \"typeUrgence\": \"accident\",
      \"localite\": \"bamako\",
      \"latitude\": \"12.5945793\",
      \"longitude\": \"-7.9190065\",
      \"priorite\": \"haute\"
    }
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 207 ]; then
    echo -e "${GREEN}✅ Succès (HTTP $HTTP_CODE)${NC}"
    echo -e "${GREEN}Réponse:${NC}"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}❌ Échec (HTTP $HTTP_CODE)${NC}"
    echo -e "${RED}Réponse:${NC}"
    echo "$BODY"
fi

echo ""
sleep 2

# Test 3: Notification à plusieurs tokens
echo -e "${BLUE}━━━ Test 3: Notification à plusieurs tokens ━━━${NC}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$LAMBDA_URL" \
  -H "Content-Type: application/json" \
  -H "x-api-key: $API_KEY" \
  -d "{
    \"tokens\": [\"$FCM_TOKEN\"],
    \"title\": \"🚨 Test Multi-tokens\",
    \"body\": \"Test d'envoi à plusieurs tokens\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 207 ]; then
    echo -e "${GREEN}✅ Succès (HTTP $HTTP_CODE)${NC}"
    echo -e "${GREEN}Réponse:${NC}"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}❌ Échec (HTTP $HTTP_CODE)${NC}"
    echo -e "${RED}Réponse:${NC}"
    echo "$BODY"
fi

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Tests terminés${NC}"
echo ""
echo -e "${YELLOW}📝 Vérifiez votre appareil pour voir si les notifications sont arrivées${NC}"
echo -e "${YELLOW}📊 Consultez aussi les logs CloudWatch:${NC}"
echo "   aws logs tail /aws/lambda/send_notification --follow"


