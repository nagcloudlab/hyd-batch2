# Reactive Programming Demo: Why We Need It

## The Problem

Traditional Spring MVC uses **one thread per request**. Under I/O-heavy workloads (DB calls, API calls), threads sit idle waiting for responses. With limited threads, new requests queue up.

```
Blocking Model (Tomcat - 10 threads):

Request-1  ──▶ [Thread-1] ████████░░░░░░░░ (blocked 200ms waiting for DB)
Request-2  ──▶ [Thread-2] ████████░░░░░░░░ (blocked 200ms waiting for DB)
...
Request-10 ──▶ [Thread-10] ████████░░░░░░░░ (blocked 200ms waiting for DB)
Request-11 ──▶ [WAITING...] ⏳ no free thread! queued!
Request-12 ──▶ [WAITING...] ⏳ no free thread! queued!
```

## The Solution: Reactive / Non-Blocking

Spring WebFlux uses **event-loop threads** that are never blocked. During I/O wait, the thread handles other requests.

```
Reactive Model (Netty - 4 threads):

Request-1  ──▶ [Thread-1] ██░░██  (starts, releases during I/O, resumes)
Request-5  ──▶ [Thread-1] ░░██░░██  (same thread handles another request!)
Request-9  ──▶ [Thread-1] ░░░░██░░██
...
All 50 requests start immediately — no queuing!
```

## Running the Demo

### Step 1: Start Blocking API (Terminal 1)
```bash
cd blocking-api
./mvnw spring-boot:run
```
Runs on port **8081** with only **10 Tomcat threads**.

### Step 2: Start Reactive API (Terminal 2)
```bash
cd reactive-api
./mvnw spring-boot:run
```
Runs on port **8082** with **4 Netty event-loop threads**.

### Step 3: Run Load Test (Terminal 3)
```bash
chmod +x load-test.sh
./load-test.sh
```

## Expected Results

| Metric | Blocking (10 threads) | Reactive (4 threads) |
|--------|----------------------|---------------------|
| Requests/sec | ~50 | ~200+ |
| Avg response time | ~1000ms | ~250ms |
| Threads used | 10 (all blocked) | 4 (never blocked) |
| Failed requests | possible under higher load | 0 |

## Key Takeaway

> **Reactive programming is NOT about being faster per request.
> It's about handling MORE concurrent requests with FEWER resources.**

### When to use Reactive:
- High-concurrency APIs (1000+ concurrent users)
- I/O-heavy services (DB calls, HTTP calls, file I/O)
- Microservices with many downstream calls
- Streaming data (SSE, WebSocket)

### When NOT to use Reactive:
- CPU-heavy computation (reactive won't help)
- Simple CRUD with low traffic
- Team not familiar with reactive patterns (learning curve)

## Tech Stack Comparison

| Layer | Blocking | Reactive |
|-------|----------|----------|
| Web | Spring MVC (Tomcat) | Spring WebFlux (Netty) |
| Data | Spring Data JPA (JDBC) | Spring Data R2DBC |
| Return types | `List<T>`, `T` | `Flux<T>`, `Mono<T>` |
| DB driver | JDBC (blocking) | R2DBC (non-blocking) |
