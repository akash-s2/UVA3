
# Payment Service — Postman Testing Guide

 

## Prerequisites

 

### 1. Fix pom.xml (Required)

 

> [!CAUTION]
> The dependency `spring-boot-h2console` in your `pom.xml` is **invalid** — it doesn't exist in Spring Boot. You must remove it or the project won't compile.

 

Remove this block from `pom.xml`:
```xml
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-h2console</artifactId>
</dependency>
```

 

The H2 console already works via the `com.h2database:h2` dependency + `spring.h2.console.enabled=true`.

 

### 2. Start the Payment Service

 

Run from IntelliJ (right-click `PaymentServiceApplication.java` → Run) or via terminal:
```bash
cd /Users/akashnandan/Desktop/UrbanVogue-Apperal-main/backend/payment-service
./mvnw spring-boot:run
```

 

You should see `Hii it is Payment service` in the console. The service runs on **port 8093**.

 

---

 

## Available Endpoint

 

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `http://localhost:8093/payment/process` | Process a payment |

 

---

 

## Test Scenarios

 

---

 

### Test 1: ✅ Successful Payment

 

**Request:**
- Method: `POST`
- URL: `http://localhost:8093/payment/process`
- Headers: `Content-Type: application/json`
- Body (raw JSON):

 

```json
{
    "orderId": "ORD-001",
    "amount": 1500.00,
    "idempotencyKey": "idem-test-001"
}
```

 

**Expected Response** (200 OK):
```json
{
    "status": "SUCCESS"
}
```
> **Note:** The payment has a 70% chance of `SUCCESS` and 30% chance of `FAILED` (random mock). Both are valid responses.

 

---

 

### Test 2: 🔁 Idempotency Check (Duplicate Request)

 

Send the **exact same request** as Test 1 again (same `idempotencyKey`):

 

**Request:**
```json
{
    "orderId": "ORD-001",
    "amount": 1500.00,
    "idempotencyKey": "idem-test-001"
}
```

 

**Expected Response** (200 OK):
```json
{
    "status": "SUCCESS"
}
```
> The response should return the **same status** as Test 1 (not re-processed). This proves idempotency is working — no duplicate payment is created in the DB.

 

---

 

### Test 3: 🚫 Malformed JSON (Exception Handling)

 

**Request:**
- Method: `POST`
- URL: `http://localhost:8093/payment/process`
- Headers: `Content-Type: application/json`
- Body (raw — intentionally broken JSON):

 

```
{ "orderId": "ORD-002", "amount": INVALID }
```

 

**Expected Response** (400 Bad Request):
```json
{
    "message": "Malformed JSON request",
    "status": 400,
    "timestamp": "2026-03-31T10:15:00"
}
```
> This tests the `HttpMessageNotReadableException` handler in `GlobalExceptionHandler`.

 

---

 

### Test 4: 🚫 Empty Body

 

**Request:**
- Method: `POST`
- URL: `http://localhost:8093/payment/process`
- Headers: `Content-Type: application/json`
- Body: *(leave completely empty — no body at all)*

 

**Expected Response** (400 Bad Request):
```json
{
    "message": "Malformed JSON request",
    "status": 400,
    "timestamp": "2026-03-31T10:15:00"
}
```

 

---

 

### Test 5: 🚫 Missing/Null Fields

 

**Request:**
```json
{
    "orderId": null,
    "amount": null,
    "idempotencyKey": null
}
```

 

**Expected Response** (500 Internal Server Error):
```json
{
    "message": "Payment processing failed: ...",
    "status": 500,
    "timestamp": "2026-03-31T10:15:00"
}
```

 

> [!NOTE]
> This returns 500 (caught by `PaymentProcessingException`) rather than 400 because the controller currently **does not have `@Valid`** on `@RequestBody`, so the `@NotNull` annotations in the DTO are not enforced. The `GlobalExceptionHandler` generic fallback catches the resulting `NullPointerException` and wraps it.

 

---

 

### Test 6: ✅ Second Payment with New Idempotency Key

 

**Request:**
```json
{
    "orderId": "ORD-002",
    "amount": 2500.00,
    "idempotencyKey": "idem-test-002"
}
```

 

**Expected Response** (200 OK):
```json
{
    "status": "SUCCESS"
}
```
> Uses a different idempotency key, so a new payment is created.

 

---

 

## Verify Data in H2 Console

 

After running the tests, you can inspect the database directly:

 

1. Open browser: **http://localhost:8093/h2-console**
2. Enter connection details:
   - **JDBC URL:** `jdbc:h2:file:../data/UrbanVogueDB1;AUTO_SERVER=TRUE;`
   - **Username:** `dp`
   - **Password:** *(leave empty)*
3. Click **Connect**
4. Run this SQL:
```sql
SELECT * FROM PAYMENTS;
```

 

You should see the persisted payments with columns: `ID`, `REQUEST_ID`, `AMOUNT`, `STATUS`, `IDEMPOTENCY_KEY`, `TRANSACTION_ID`, `PAYMENT_MODE`, `CREATED_AT`.

 

---

 

## Quick Reference: Exception Handling Responses

 

| Scenario | HTTP Status | Handler Used |
|----------|-------------|--------------|
| Malformed JSON | **400** | `HttpMessageNotReadableException` |
| Validation error (if `@Valid` added later) | **400** | `MethodArgumentNotValidException` |
| Duplicate idempotency key (DB constraint) | **409** | `DataIntegrityViolationException` |
| DB or processing failure | **500** | `PaymentProcessingException` |
| Any other unexpected error | **500** | Generic `Exception` |

