#!/bin/bash

echo "Stopping OpenTelemetry demo..."

# Stop Spring Boot services
if [ -f /tmp/otel-demo-pids.txt ]; then
    read PRODUCT_PID ORDER_PID < /tmp/otel-demo-pids.txt
    kill $PRODUCT_PID 2>/dev/null && echo "  product-service stopped"
    kill $ORDER_PID 2>/dev/null && echo "  order-service stopped"
    rm /tmp/otel-demo-pids.txt
else
    pkill -f "product-service" 2>/dev/null
    pkill -f "order-service" 2>/dev/null
    echo "  services stopped"
fi

# Stop Jaeger
cd "$(dirname "$0")"
docker compose down
echo "  Jaeger stopped"
echo "Done."
