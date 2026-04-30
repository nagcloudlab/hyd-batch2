#!/bin/bash
# Load test: Place 100 orders against the FTGO monolith
# Usage: ./load-test.sh [base_url]

BASE_URL="${1:-http://localhost:8080}"

# Seed data arrays (matching data.sql)
CONSUMERS=(1 2 3)
RESTAURANTS=(1 2 3)
# Menu items per restaurant: 1→[1-5], 2→[6-10], 3→[11-15]
MENU_ITEMS_1=(1 2 3 4 5)
MENU_ITEMS_2=(6 7 8 9 10)
MENU_ITEMS_3=(11 12 13 14 15)
ADDRESSES=("Marine+Drive+Mumbai" "FC+Road+Pune" "Colaba+Mumbai" "Bandra+Mumbai" "Kothrud+Pune")

echo "Placing 100 orders against $BASE_URL ..."
echo ""

SUCCESS=0
FAIL=0

for i in $(seq 1 100); do
  # Pick random consumer, restaurant, address
  CONSUMER=${CONSUMERS[$((RANDOM % 3))]}
  RESTAURANT=${RESTAURANTS[$((RANDOM % 3))]}
  ADDRESS=${ADDRESSES[$((RANDOM % 5))]}

  # Pick 1-3 random menu items from that restaurant
  case $RESTAURANT in
    1) ITEMS=("${MENU_ITEMS_1[@]}") ;;
    2) ITEMS=("${MENU_ITEMS_2[@]}") ;;
    3) ITEMS=("${MENU_ITEMS_3[@]}") ;;
  esac

  NUM_ITEMS=$(( (RANDOM % 3) + 1 ))
  ITEM_PARAMS=""
  for j in $(seq 1 $NUM_ITEMS); do
    ITEM=${ITEMS[$((RANDOM % 5))]}
    QTY=$(( (RANDOM % 3) + 1 ))
    ITEM_PARAMS="${ITEM_PARAMS}&menuItemIds=${ITEM}&quantities=${QTY}"
  done

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/consumer/orders" \
    -d "consumerId=${CONSUMER}&restaurantId=${RESTAURANT}${ITEM_PARAMS}&deliveryAddress=${ADDRESS}")

  if [ "$HTTP_CODE" = "302" ]; then
    SUCCESS=$((SUCCESS + 1))
  else
    FAIL=$((FAIL + 1))
  fi

  printf "\rOrder %3d/100  success=%d  fail=%d" "$i" "$SUCCESS" "$FAIL"
done

echo ""
echo ""
echo "Done! $SUCCESS succeeded, $FAIL failed."
echo ""
echo "Check Kafka UI:  http://localhost:9090"
echo "Check consumer lag and messages in topic: ftgo.notification.events"
