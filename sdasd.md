# 🛍️ UrbanVogue Apparel — Backend Microservices

> A scalable, secure e-commerce backend platform for the UrbanVogue apparel brand, built on a microservices architecture using **Java 21**, **Spring Boot 4.x / 3.x**, **Spring Cloud Gateway**, **Spring Security + JWT**, and **H2 (shared file-mode database)**.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Microservices](#microservices)
   - [API Gateway](#1-api-gateway-port-8090)
   - [User Service](#2-user-service-port-8091)
   - [Admin Service](#3-admin-service-port-8092)
   - [Payment Service](#4-payment-service-port-8093)
4. [Database Design](#database-design)
5. [Authentication & Authorization](#authentication--authorization)
6. [API Reference](#api-reference)
7. [Inter-Service Communication](#inter-service-communication)
8. [Error Handling](#error-handling)
9. [Getting Started](#getting-started)
10. [Project Structure](#project-structure)
11. [Configuration](#configuration)

---

## Architecture Overview

```
                        ┌───────────────────────────────────┐
                        │           React Frontend          │
                        │         (localhost:3000)           │
                        └──────────────┬────────────────────┘
                                       │
                                       ▼
                        ┌───────────────────────────────────┐
                        │     API Gateway (port 8090)       │
                        │  ● JWT validation (Global Filter) │
                        │  ● Route-based proxying           │
                        │  ● CORS configuration             │
                        │  ● Spring Cloud Gateway           │
                        └─────┬──────────┬──────────┬───────┘
                              │          │          │
              ┌───────────────┤          │          ├───────────────┐
              ▼               ▼          ▼          ▼               │
  ┌────────────────┐  ┌──────────────┐  ┌──────────────────┐       │
  │  User Service  │  │ Admin Service│  │ Payment Service  │       │
  │  (port 8091)   │  │ (port 8092)  │  │ (port 8093)      │       │
  │                │  │              │  │                   │       │
  │ ● Auth Module  │  │ ● Products   │  │ ● Process Payment│       │
  │ ● Order Module │  │ ● Catalog    │  │ ● Idempotency    │       │
  │ ● Cart Module  │  │ ● Inventory  │  │ ● Persistence    │       │
  │ ● Product View │  │ ● Updates    │  │                   │       │
  └───────┬────────┘  └──────┬───────┘  └──────────────────┘       │
          │                  │                                      │
          │    Inter-Service │ (RestTemplate / Headers)             │
          │◄─────────────────┤                                      │
          │─────────────────►│                                      │
          └──────────────────┴──────────────────────────────────────┘
                                       │
                                       ▼
                        ┌───────────────────────────────────┐
                        │   Shared H2 Database (file mode)  │
                        │   ../data/UrbanVogueDB1           │
                        └───────────────────────────────────┘
```

---

## Technology Stack

| Layer               | Technology                                                     |
| ------------------- | -------------------------------------------------------------- |
| **Language**         | Java 21                                                        |
| **Framework**        | Spring Boot 4.0.3 (User, Admin, Payment) · Spring Boot 3.3.5 (Gateway) |
| **API Gateway**      | Spring Cloud Gateway (reactive, Netty-based)                   |
| **Security**         | Spring Security · JWT (HMAC-SHA256, jjwt 0.11.5)              |
| **Data**             | Spring Data JPA · Hibernate · H2 (shared file-mode)           |
| **Inter-Service**    | RestTemplate · Spring Cloud OpenFeign (declared)               |
| **Validation**       | Jakarta Bean Validation (`spring-boot-starter-validation`)     |
| **Build**            | Apache Maven · Maven Wrapper                                   |
| **Utilities**        | Lombok · Spring Boot DevTools · Actuator                       |
| **Caching**          | Spring Cache (`spring-boot-starter-cache`) — Admin Service     |

---

## Microservices

### 1. API Gateway (port 8090)

The single entry point for all client requests. It performs **JWT-based authentication** at the edge and proxies requests to downstream services.

**Key Components:**

| File | Purpose |
|------|---------|
| `ApiGatewayApplication.java` | Spring Boot application entry point |
| `filter/AuthFilter.java` | Global gateway filter — validates JWT tokens, extracts email & role, sets reactive security context |
| `security/JwtUtil.java` | JWT utility — validates tokens and extracts claims (email, role) |
| `security/SecurityConfig.java` | WebFlux security — CSRF disabled, public endpoints whitelisted |
| `config/CorsConfig.java` | CORS policy — allows `localhost:3000` with credentials |

**Route Configuration** (`application.yml`):

| Route ID | Path Pattern | Target |
|----------|-------------|--------|
| `auth-service` | `/auth/**` | User Service (8091) |
| `user-service` | `/user/**` | User Service (8091) |
| `user-service-admin-change` | `/change/admin/**` | User Service (8091) |
| `admin-service` | `/admin/**` | Admin Service (8092) |
| `catalog-service` | `/catalog/**` | Admin Service (8092) |

**Public Endpoints** (skip JWT validation):
- `/auth/**` — Login & registration
- `/user/getProducts/**` — Product browsing
- `/catalog/**` — Product catalog

---

### 2. User Service (port 8091)

The core consumer-facing service handling authentication, order management, cart checkout, and product browsing.

**Modules:**

#### Auth Module (`com.UrbanVogue.user.AuthModule`)
Handles user registration, login, and JWT token generation.

| File | Purpose |
|------|---------|
| `controller/AuthController.java` | `POST /auth/register` · `POST /auth/login` |
| `entity/User.java` | JPA entity → `users` table (id, name, email, password, phoneNumber, address, role) |
| `repository/UserRepository.java` | JPA repository with `findByEmail()` |
| `service/AuthService.java` | Registration (BCrypt hashing, duplicate check) · Login (password verification, JWT generation) |
| `service/AdminInitializer.java` | `CommandLineRunner` — seeds a default admin user (`admin@urbanvogue.com` / `admin123`) on startup |
| `dto/RegisterRequest.java` | Name, email, password, phoneNumber, address |
| `dto/LoginRequest.java` | Email, password |
| `dto/AuthResponse.java` | Message, token |

#### Order Module (`com.UrbanVogue.user.OrderModule`)
Handles single-item orders and cart-based multi-item checkout.

| File | Purpose |
|------|---------|
| `controller/OrderController.java` | `POST /user/orders/place` · `GET /user/orders/my-orders` |
| `controller/CartController.java` | `POST /user/orders/cart` |
| `controller/AdminOrderController.java` | `PUT /change/admin/{orderId}/status` (Admin only) |
| `entity/Order.java` | JPA entity → `orders` table (id, productId, productName, customerEmail, quantity, price, totalAmount, address, paymentStatus, cartId, orderStatus, createdAt) |
| `repository/OrderRepository.java` | JPA repository with `findByCustomerEmail()` |
| `service/OrderService.java` | Place order flow: validate stock → create order → process payment → reduce stock → persist |
| `service/CartService.java` | Multi-item cart: per-item stock check → aggregate payment → batch stock reduction |
| `client/ProductClient.java` | RestTemplate client → calls Admin Service internal APIs for product/stock |
| `client/PaymentClient.java` | RestTemplate client → calls Payment Service for payment processing |

**Order Flow (Single Item):**
```
1. Extract email from JWT token
2. Fetch product details from Admin Service (/internal/products/{id}?qty=N)
3. Validate stock availability
4. Calculate total amount
5. Save order (initial state)
6. Generate idempotency key
7. Call Payment Service (/payment/process)
8. If payment SUCCESS → reduce stock → update order status to BOOKED
9. If payment FAILED → update order status to FAILED
```

**Order Status Lifecycle:**
```
BOOKED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
```

#### Product Module (`com.UrbanVogue.user.ProductModule`)
Public product browsing interface.

| File | Purpose |
|------|---------|
| `controller/GetProductController.java` | `GET /user/getProducts` · `GET /user/getProducts/{id}` |
| `service/GetProductService.java` | Fetches products via RestTemplate from Admin Service catalog |

#### Security & Cross-Cutting Concerns

| File | Purpose |
|------|---------|
| `security/SecurityConfig.java` | HTTP security chain — JWT filter, role-based access (`USER` for orders, `ADMIN` for order status updates) |
| `security/JwtUtil.java` | Full JWT lifecycle: generate (email+role), extract claims, validate |
| `filter/JwtAuthFilter.java` | Servlet filter — extracts JWT from `Authorization` header, sets `SecurityContext` |
| `exception/GlobalExceptionHandler.java` | `@RestControllerAdvice` — handles CustomException, ResourceNotFoundException, UnauthorizedException |
| `exception/ErrorResponse.java` | Standardized error response (message, status code) |
| `config/AppConfig.java` | Application-level beans configuration |

---

### 3. Admin Service (port 8092)

Back-office service for product catalog management, inventory control, and internal APIs consumed by the User Service.

**Modules:**

#### Product Management (`addProduct`)

| File | Purpose |
|------|---------|
| `controller/ProductController.java` | `POST /admin/products/add` · `DELETE /admin/products/delete/{id}` |
| `entity/Product.java` | JPA entity → `product` table (id, name, brand, category, size, color, price, imageUrl, description, createdAt) |
| `repository/ProductRepository.java` | Standard JPA repository |
| `service/ProductService.java` | Add product (creates Product + Inventory) · Delete product (cascades to Inventory) |

#### Product Updates (`UpdateDetails`)

| File | Purpose |
|------|---------|
| `controller/UpdateProductController.java` | `PATCH /admin/products/update/{id}` |
| `dto/UpdateProductRequestDTO.java` | Partial update fields |
| `service/UpdateProductService.java` | Partial product update logic |

#### Inventory Management (`Inventory`)

| File | Purpose |
|------|---------|
| `controller/InventoryController.java` | `PUT /admin/inventory/{productId}?numberOfPieces=N` · `GET /admin/inventory/products` |
| `entity/Inventory.java` | JPA entity → `inventory` table (productId as PK, numberOfPieces) |
| `repository/InventoryRepository.java` | JPA repository with `findByProductId()` |
| `service/InventoryService.java` | Stock update and retrieval |

#### Public Catalog (`catalog`)

| File | Purpose |
|------|---------|
| `controller/CatalogController.java` | `GET /catalog/getProducts?page=0&size=3` · `GET /catalog/search?category=X` · `GET /catalog/getProducts/{id}` |
| `dto/CatalogProductDTO.java` | Public product view (no sensitive fields) |
| `service/CatalogService.java` | Paginated product listing and category search |

#### Internal APIs (`orderHandling`)
*Consumed by User Service during order placement — not exposed externally.*

| File | Purpose |
|------|---------|
| `controller/InternalController.java` | `GET /internal/products/{id}?qty=N` · `PUT /internal/products/reduce/{id}?qty=N` |
| `dto/ProductInternalDTO.java` | Price, available quantity, product name |
| `service/InternalService.java` | Stock availability check and reduction |

#### Security & Configuration

| File | Purpose |
|------|---------|
| `security/SecurityConfig.java` | Stateless sessions, `/admin/**` requires ADMIN role, `/catalog/**` and `/internal/**` are public |
| `security/JwtUtil.java` | JWT claim extraction (same logic as gateway) |
| `filter/JwtAuthFilter.java` | JWT servlet filter for admin request authentication |
| `config/CacheConfig.java` | Spring Cache configuration |

---

### 4. Payment Service (port 8093)

Processes payment requests with **idempotency protection** and **persistence**.

| File | Purpose |
|------|---------|
| `controller/PaymentController.java` | `POST /payment/process` |
| `entity/Payment.java` | JPA entity → `payments` table (id, requestID, amount, status, idempotencyKey, transactionId, paymentMode, createdAt) |
| `repository/PaymentRepository.java` | JPA repository with `findByIdempotencyKey()` |
| `service/PaymentService.java` | Idempotent payment processing (70% simulated success rate) |
| `exception/GlobalExceptionHandler.java` | Handles PaymentNotFoundException, PaymentProcessingException, validation errors, malformed JSON, DB constraint violations |

**Payment Processing Flow:**
```
1. Validate idempotency key is present
2. Check if idempotency key already exists → return cached result
3. Simulate payment (70% success probability)
4. Assign payment mode (50% UPI / 50% Internet Banking)
5. Generate unique transaction ID
6. Persist payment record
7. Handle race conditions via DataIntegrityViolationException catch
```

**Idempotency:** Payments use a unique `idempotencyKey` (per request) with a `UNIQUE` database constraint to prevent duplicate charges.

---

## Database Design

All services share a single H2 database file (`../data/UrbanVogueDB1`) in auto-server mode, allowing concurrent access from multiple JVMs.

### Entity Relationship Diagram

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│      users       │       │     product       │       │    inventory     │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK, auto)    │       │ id (PK, auto)    │◄──────│ productId (PK)   │
│ name             │       │ name             │  1:1  │ numberOfPieces   │
│ email (unique)   │       │ brand            │       └──────────────────┘
│ password (bcrypt)│       │ category         │
│ phoneNumber      │       │ size             │
│ address          │       │ color            │
│ role (USER/ADMIN)│       │ price            │
└──────────────────┘       │ imageUrl         │
                           │ description      │
                           │ createdAt        │
                           └──────────────────┘

┌──────────────────┐       ┌──────────────────┐
│      orders      │       │    payments      │
├──────────────────┤       ├──────────────────┤
│ id (PK, auto)    │       │ id (PK, auto)    │
│ productId        │       │ requestID        │
│ productName      │       │ amount           │
│ customerEmail    │       │ status           │
│ quantity         │       │ idempotencyKey   │
│ price            │       │   (unique)       │
│ totalAmount      │       │ transactionId    │
│ address          │       │ paymentMode      │
│ paymentStatus    │       │ createdAt        │
│ cartId           │       └──────────────────┘
│ orderStatus      │
│ createdAt        │
└──────────────────┘
```

---

## Authentication & Authorization

### JWT Token Structure

```json
{
  "sub": "user@example.com",
  "role": "USER",
  "iat": 1712345678,
  "exp": 1712604878
}
```

- **Algorithm:** HMAC-SHA256
- **Secret:** Shared across all services via `jwt.secret` property
- **Expiration:** 72 hours (259,200,000 ms)

### Authentication Flow

```
┌────────┐     POST /auth/login      ┌──────────────┐
│ Client │ ─────────────────────────► │ User Service │
│        │ ◄───────────────────────── │ (AuthModule) │
│        │     { token: "eyJ..." }    └──────────────┘
│        │
│        │     GET /user/orders/my-orders
│        │     Authorization: Bearer eyJ...
│        │ ─────────────────────────► ┌──────────────┐
│        │                            │  API Gateway │
│        │                            │ (AuthFilter) │
│        │                            │  ✓ validate  │
│        │                            │  ✓ extract   │
│        │                            │    email+role│
│        │                            └──────┬───────┘
│        │                                   │ proxy
│        │                                   ▼
│        │                            ┌──────────────┐
│        │ ◄────────────────────────  │ User Service │
│        │     [order1, order2, ...]  │ (JwtAuthFilter)
└────────┘                            └──────────────┘
```

### Role-Based Access Control

| Role    | Accessible Endpoints                                                                |
| ------- | ----------------------------------------------------------------------------------- |
| `PUBLIC`| `/auth/**` · `/catalog/**` · `/user/getProducts/**`                                 |
| `USER`  | `/user/orders/**` (place orders, view my orders, cart checkout)                      |
| `ADMIN` | `/admin/**` (product CRUD, inventory management) · `/change/admin/**` (order status) |

### Default Admin Account
On first startup, the `AdminInitializer` seeds a default admin user:
- **Email:** `admin@urbanvogue.com`
- **Password:** `admin123`

---

## API Reference

### Auth APIs (`/auth`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/auth/register` | Public | Register a new user |
| `POST` | `/auth/login` | Public | Login and receive JWT token |

<details>
<summary><strong>POST /auth/register</strong> — Request & Response</summary>

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "phoneNumber": "9876543210",
  "address": "123 Main Street, City"
}
```

**Response (Success):**
```json
{
  "message": "User registered successfully",
  "token": null
}
```

**Response (Duplicate Email):**
```json
{
  "message": "Email already registered",
  "token": null
}
```
</details>

<details>
<summary><strong>POST /auth/login</strong> — Request & Response</summary>

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response (Success):**
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```
</details>

---

### User / Order APIs (`/user`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/user/getProducts` | Public | Browse all products |
| `GET` | `/user/getProducts/{id}` | Public | Get product details |
| `POST` | `/user/orders/place` | `USER` | Place a single-item order |
| `POST` | `/user/orders/cart` | `USER` | Place a multi-item cart order |
| `GET` | `/user/orders/my-orders` | `USER` | View user's order history |

<details>
<summary><strong>POST /user/orders/place</strong> — Request & Response</summary>

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 2,
  "address": "456 Elm Street, City"
}
```

**Response (Success):**
```json
{
  "message": "Order placed successfully",
  "paymentStatus": "SUCCESS",
  "orderStatus": "BOOKED"
}
```
</details>

<details>
<summary><strong>POST /user/orders/cart</strong> — Request & Response</summary>

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ],
  "address": "789 Oak Avenue, City"
}
```

**Response:**
```json
{
  "products": [
    {
      "productId": 1,
      "productName": "Classic T-Shirt",
      "quantity": 2,
      "price": 499.0,
      "orderStatus": "BOOKED",
      "paymentStatus": "SUCCESS"
    },
    {
      "productId": 3,
      "productName": "Denim Jacket",
      "quantity": 1,
      "price": 1999.0,
      "orderStatus": "BOOKED",
      "paymentStatus": "SUCCESS"
    }
  ],
  "totalAmount": 2997.0
}
```
</details>

---

### Admin APIs (`/admin`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/admin/products/add` | `ADMIN` | Add a new product |
| `DELETE` | `/admin/products/delete/{id}` | `ADMIN` | Delete a product |
| `PATCH` | `/admin/products/update/{id}` | `ADMIN` | Partially update a product |
| `PUT` | `/admin/inventory/{productId}?numberOfPieces=N` | `ADMIN` | Update stock quantity |
| `GET` | `/admin/inventory/products` | `ADMIN` | View all inventory |
| `PUT` | `/change/admin/{orderId}/status?value=SHIPPED` | `ADMIN` | Update order status |

<details>
<summary><strong>POST /admin/products/add</strong> — Request & Response</summary>

**Headers:** `Authorization: Bearer <admin-token>`

**Request Body:**
```json
{
  "name": "Classic T-Shirt",
  "brand": "UrbanVogue",
  "category": "T-Shirts",
  "size": "M",
  "color": "Navy Blue",
  "price": 499.0,
  "imageUrl": "https://example.com/tshirt.jpg",
  "description": "Premium cotton classic-fit t-shirt",
  "numberOfPieces": 100
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Classic T-Shirt",
  "message": "Product added successfully"
}
```
</details>

---

### Catalog APIs (`/catalog`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/catalog/getProducts?page=0&size=3` | Public | Paginated product listing |
| `GET` | `/catalog/search?category=T-Shirts&page=0&size=3` | Public | Search products by category |
| `GET` | `/catalog/getProducts/{id}` | Public | Get full product details |

---

### Internal APIs (`/internal`) — Service-to-Service Only

| Method | Endpoint | Caller | Description |
|--------|----------|--------|-------------|
| `GET` | `/internal/products/{id}?qty=N` | User Service | Get product price, stock, name |
| `PUT` | `/internal/products/reduce/{id}?qty=N` | User Service | Reduce stock after purchase |

---

### Payment API (`/payment`) — Service-to-Service Only

| Method | Endpoint | Caller | Description |
|--------|----------|--------|-------------|
| `POST` | `/payment/process` | User Service | Process payment with idempotency |

<details>
<summary><strong>POST /payment/process</strong> — Request & Response</summary>

**Request Body:**
```json
{
  "orderId": "abc12",
  "amount": 998.0,
  "idempotencyKey": "xyz1234567"
}
```

**Response:**
```json
{
  "status": "SUCCESS"
}
```
</details>

---

## Inter-Service Communication

All inter-service calls use **RestTemplate** over HTTP (synchronous).

```
User Service ──────────────────────► Admin Service
  ProductClient.getProduct()          GET  /internal/products/{id}?qty=N
  ProductClient.reduceStock()         PUT  /internal/products/reduce/{id}?qty=N

User Service ──────────────────────► Payment Service
  PaymentClient.processPayment()      POST /payment/process
```

**Security for inter-service calls:** The User Service forwards user identity via custom headers (`X-User-Email`, `X-User-Role`) extracted from the SecurityContext, ensuring downstream services can enforce authorization.

---

## Error Handling

Both the **User Service** and **Payment Service** implement global exception handling via `@RestControllerAdvice`.

### User Service Error Responses

| Exception | HTTP Status | When |
|-----------|------------|------|
| `CustomException` | `400 Bad Request` | Business logic violations |
| `ResourceNotFoundException` | `404 Not Found` | Entity not found |
| `UnauthorizedException` | `401 Unauthorized` | Authentication failures |
| `Exception` (fallback) | `500 Internal Server Error` | Unexpected errors |

### Payment Service Error Responses

| Exception | HTTP Status | When |
|-----------|------------|------|
| `PaymentNotFoundException` | `404 Not Found` | Payment record not found |
| `PaymentProcessingException` | `500 Internal Server Error` | Payment processing failure |
| `MethodArgumentNotValidException` | `400 Bad Request` | Validation errors |
| `HttpMessageNotReadableException` | `400 Bad Request` | Malformed JSON |
| `DataIntegrityViolationException` | `409 Conflict` | Duplicate idempotency key |
| `Exception` (fallback) | `500 Internal Server Error` | Unexpected errors |

**Standard Error Response Format:**
```json
{
  "message": "Descriptive error message",
  "status": 400
}
```

---

## Getting Started

### Prerequisites

- **JDK 21** (or later)
- **Maven** (or use the included Maven Wrapper `./mvnw`)
- No external database setup needed — uses embedded H2

### Startup Order

Services should be started in the following order to ensure dependencies are available:

```bash
# 1. Start Admin Service (port 8092) — product data source
cd admin-service
./mvnw spring-boot:run

# 2. Start Payment Service (port 8093) — payment processing
cd payment-service
./mvnw spring-boot:run

# 3. Start User Service (port 8091) — depends on Admin + Payment
cd user-service
./mvnw spring-boot:run

# 4. Start API Gateway (port 8090) — entry point
cd api-gateway
./mvnw spring-boot:run
```

### Verify Services

| Service | Health Check URL |
|---------|-----------------|
| API Gateway | `http://localhost:8090/actuator/health` |
| User Service | `http://localhost:8091/actuator/health` |
| Admin Service | `http://localhost:8092/actuator/health` |
| Payment Service | `http://localhost:8093/actuator/health` |

### H2 Console Access

Each service exposes an H2 web console for database inspection:

| Service | H2 Console URL | JDBC URL |
|---------|----------------|----------|
| User Service | `http://localhost:8091/h2-console` | `jdbc:h2:file:../data/UrbanVogueDB1` |
| Admin Service | `http://localhost:8092/h2-console` | `jdbc:h2:file:../data/UrbanVogueDB1` |
| Payment Service | `http://localhost:8093/h2-console` | `jdbc:h2:file:../data/UrbanVogueDB1` |

**Credentials:** Username: `dp` · Password: *(empty)*

---

## Project Structure

```
backend/
├── api-gateway/                          # Spring Cloud Gateway (port 8090)
│   └── src/main/java/com/UrbanVogue/gateway/
│       ├── ApiGatewayApplication.java
│       ├── config/
│       │   └── CorsConfig.java           # CORS policy for frontend
│       ├── filter/
│       │   └── AuthFilter.java           # Global JWT authentication filter
│       └── security/
│           ├── JwtUtil.java              # JWT claim extraction
│           └── SecurityConfig.java       # WebFlux security rules
│
├── user-service/                          # User-facing service (port 8091)
│   └── src/main/java/com/UrbanVogue/user/
│       ├── UserServiceApplication.java
│       ├── AuthModule/                   # Authentication & Registration
│       │   ├── controller/AuthController.java
│       │   ├── dto/{AuthResponse, LoginRequest, RegisterRequest}.java
│       │   ├── entity/User.java
│       │   ├── repository/UserRepository.java
│       │   └── service/{AuthService, AdminInitializer}.java
│       ├── OrderModule/                  # Orders & Cart
│       │   ├── client/{ProductClient, PaymentClient}.java
│       │   ├── config/RestTemplateConfig.java
│       │   ├── controller/{OrderController, CartController, AdminOrderController}.java
│       │   ├── dto/{OrderRequestDTO, OrderResponseDTO, CartRequestDTO, CartResponseDTO, ...}.java
│       │   ├── entity/Order.java
│       │   ├── repository/OrderRepository.java
│       │   └── service/{OrderService, CartService}.java
│       ├── ProductModule/                # Public product browsing
│       │   ├── controller/GetProductController.java
│       │   ├── dto/GetProductDTO.java
│       │   └── service/GetProductService.java
│       ├── config/AppConfig.java
│       ├── exception/{GlobalExceptionHandler, ErrorResponse, CustomException, ...}.java
│       ├── filter/JwtAuthFilter.java
│       └── security/{JwtUtil, SecurityConfig}.java
│
├── admin-service/                         # Admin back-office (port 8092)
│   └── src/main/java/com/UrbanVogue/admin/
│       ├── AdminServiceApplication.java
│       ├── addProduct/                   # Product CRUD
│       │   ├── controller/ProductController.java
│       │   ├── dto/{ProductRequestDTO, ProductResponseDTO}.java
│       │   ├── entity/Product.java
│       │   ├── repository/ProductRepository.java
│       │   └── service/ProductService.java
│       ├── UpdateDetails/                # Partial product updates
│       │   ├── controller/UpdateProductController.java
│       │   ├── dto/UpdateProductRequestDTO.java
│       │   └── service/UpdateProductService.java
│       ├── Inventory/                    # Stock management
│       │   ├── controller/InventoryController.java
│       │   ├── dto/InventoryProductDTO.java
│       │   ├── entity/Inventory.java
│       │   ├── repository/InventoryRepository.java
│       │   └── service/InventoryService.java
│       ├── catalog/                      # Public product catalog
│       │   ├── controller/CatalogController.java
│       │   ├── dto/CatalogProductDTO.java
│       │   └── service/CatalogService.java
│       ├── orderHandling/                # Internal APIs for User Service
│       │   ├── controller/InternalController.java
│       │   ├── dto/ProductInternalDTO.java
│       │   └── service/InternalService.java
│       ├── config/CacheConfig.java
│       ├── filter/JwtAuthFilter.java
│       └── security/{JwtUtil, SecurityConfig}.java
│
├── payment-service/                       # Payment processing (port 8093)
│   └── src/main/java/com/UrbanVogue/payment/
│       ├── PaymentServiceApplication.java
│       ├── controller/PaymentController.java
│       ├── dto/{PaymentRequestDTO, PaymentResponseDTO}.java
│       ├── entity/Payment.java
│       ├── repository/PaymentRepository.java
│       ├── service/PaymentService.java
│       └── exception/{GlobalExceptionHandler, ErrorResponse, PaymentNotFoundException, PaymentProcessingException}.java
│
└── data/                                  # Shared H2 database files (auto-generated)
    └── UrbanVogueDB1.mv.db
```

---

## Configuration

### Service Ports

| Service | Port | Spring Profile |
|---------|------|---------------|
| API Gateway | `8090` | default |
| User Service | `8091` | default |
| Admin Service | `8092` | default |
| Payment Service | `8093` | default |

### JWT Configuration

All services share the same JWT secret for token validation consistency:

```properties
jwt.secret=urbanvogue-super-secret-key-for-tokengeneration-2026-very-secure-key
jwt.expiration=259200000  # 72 hours
```

### Database Configuration

```properties
spring.datasource.url=jdbc:h2:file:../data/UrbanVogueDB1;AUTO_SERVER=TRUE;
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=dp
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

> **Note:** `AUTO_SERVER=TRUE` enables H2's automatic mixed-mode, allowing multiple services to connect to the same database file simultaneously.

---

## License

This project is developed as part of the UrbanVogue Apparel e-commerce platform.
