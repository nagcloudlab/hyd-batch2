#!/bin/bash

# ============================================================
# OpenTelemetry Demo: Distributed Tracing with 2 Microservices
# ============================================================
# Architecture:
#   [Client] --> [order-service:8082] --> [product-service:8081]
#                       |                         |
#                       +-----> [OTel Collector] <--+
#                                     |
#                               [Jaeger UI:16686]
# ============================================================

echo "========================================="
echo "  OpenTelemetry Distributed Tracing Demo"
echo "========================================="
echo ""

# Step 1: Start Jaeger
echo "[1/4] Starting Jaeger (docker)..."
cd "$(dirname "$0")"
docker compose up -d
sleep 3
echo "  Jaeger UI: http://localhost:16686"
echo ""

# Step 2: Start product-service
echo "[2/4] Starting product-service (port 8081)..."
cd product-service
./mvnw spring-boot:run > /tmp/product-service.log 2>&1 &
PRODUCT_PID=$!
cd ..

# Step 3: Start order-service
echo "[3/4] Starting order-service (port 8082)..."
cd order-service
./mvnw spring-boot:run > /tmp/order-service.log 2>&1 &
ORDER_PID=$!
cd ..

# Wait for services to start
echo "  Waiting for services to start..."
sleep 20

# Verify
if curl -s http://localhost:8081/api/products/1 > /dev/null 2>&1; then
    echo "  product-service: UP"
else
    echo "  product-service: FAILED (check /tmp/product-service.log)"
    exit 1
fi

if curl -s http://localhost:8082/api/orders > /dev/null 2>&1; then
    echo "  order-service: UP"
else
    echo "  order-service: FAILED (check /tmp/order-service.log)"
    exit 1
fi

echo ""

# Step 4: Generate traffic
echo "[4/4] Generating sample requests..."
echo ""

echo "  --> POST /api/orders (buy 2 Laptops)"
curl -s -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}' | python3 -m json.tool
echo ""

echo "  --> POST /api/orders (buy 1 Phone)"
curl -s -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 2, "quantity": 1}' | python3 -m json.tool
echo ""

echo "  --> POST /api/orders (buy 3 Tablets)"
curl -s -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 3, "quantity": 3}' | python3 -m json.tool
echo ""

echo "========================================="
echo "  DONE! Open Jaeger UI to see traces:"
echo "  http://localhost:16686"
echo ""
echo "  Select service: 'order-service'"
echo "  Click 'Find Traces'"
echo ""
echo "  You'll see distributed traces spanning:"
echo "    order-service --> product-service"
echo ""
echo "  Each trace shows:"
echo "    - HTTP call from order -> product"
echo "    - Custom spans (validate-stock, fetch-product, persist-order)"
echo "    - Timing breakdown of each operation"
echo "========================================="
echo ""
echo "  To stop: ./stop-demo.sh"
echo "  PIDs: product-service=$PRODUCT_PID, order-service=$ORDER_PID"
echo "$PRODUCT_PID $ORDER_PID" > /tmp/otel-demo-pids.txt
