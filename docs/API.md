# API Reference

Complete REST API reference for the Giftly e-commerce platform.

**Base URL:** `http://localhost:8080/api`

**Interactive Documentation:** [Swagger UI](http://localhost:8080/swagger-ui.html)

**OpenAPI Spec:** [JSON](http://localhost:8080/api-docs)

---

## Authentication

All protected endpoints require a JWT Bearer token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

### Public Endpoints

| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| `POST` | `/api/auth/login` | No |
| `POST` | `/api/auth/register` | No |
| `GET` | `/api/products` | No |
| `GET` | `/api/categories` | No |

### Protected Endpoints

All other endpoints require authentication. Admin endpoints require the `ADMIN` role.

---

## Table of Contents

1. [Authentication](#authentication-endpoints)
2. [Products](#products)
3. [Categories](#categories)
4. [User Profile](#user-profile)
5. [Shopping Cart](#shopping-cart)
6. [Orders](#orders)
7. [Reviews](#reviews)
8. [Gift Boxes](#gift-boxes)
9. [Admin — Products](#admin--products)
10. [Admin — Categories](#admin--categories)
11. [Admin — Orders](#admin--orders)
12. [Admin — Users](#admin--users)
13. [Admin — Reviews](#admin--reviews)
14. [Admin — Dashboard](#admin--dashboard)

---

## Authentication Endpoints

### Login

Authenticate with email and password to receive a JWT token.

```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "fullName": "John Doe"
    }
  }
}
```

### Register

Create a new user account.

```
POST /api/auth/register
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "fullName": "John Doe",
  "phone": "0123456789"
}
```

**Response:** Same as Login — returns JWT tokens on successful registration.

### Logout

Logout the current user. The client should discard the JWT token.

```
POST /api/auth/logout
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Logout successful. Please discard your token.",
  "data": "Logged out"
}
```

---

## Products

### Browse Products

Browse products with optional filtering and cursor-based pagination.

```
GET /api/products
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `categoryId` | UUID | No | Filter by category |
| `minPrice` | BigDecimal | No | Minimum price filter |
| `maxPrice` | BigDecimal | No | Maximum price filter |
| `keyword` | String | No | Search by name or description (fuzzy search) |
| `cursor` | String | No | Cursor for pagination |
| `pageSize` | int | No | Items per page (default: 20) |

**Example:**
```
GET /api/products?categoryId=550e8400-e29b-41d4-a716-446655440000&minPrice=10000&maxPrice=50000&keyword=chocolate&page=20
```

**Response:**
```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "name": "Chocolate Gift Box",
        "description": "Premium chocolate collection",
        "price": 25000,
        "productType": "GIFT_BOX",
        "status": "AVAILABLE",
        "category": { "id": "...", "name": "Sweets" },
        "images": [
          { "imageUrl": "https://...", "isThumbnail": true }
        ]
      }
    ],
    "nextCursor": "eyJpZCI6IjU1MGU4NDAw...",
    "hasNext": true,
    "pageSize": 20
  }
}
```

---

## Categories

### List All Categories

Retrieve all product categories (public endpoint).

```
GET /api/categories
```

**Response:**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    { "id": "550e8400-e29b-41d4-a716-446655440010", "name": "Sweets", "description": "Sweet treats and chocolates" },
    { "id": "550e8400-e29b-41d4-a716-446655440011", "name": "Wrapping", "description": "Gift wrapping materials" }
  ]
}
```

---

## User Profile

### Get Current User

Retrieve the authenticated user's profile.

```
GET /api/users/me
Authorization: Bearer <token>
```

### Update Profile

Update the authenticated user's profile information.

```
PUT /api/users/me
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "fullName": "John Updated",
  "phone": "0987654321"
}
```

### Update Admin Profile

Update profile with secret code verification (for admin users).

```
PATCH /api/users/me
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "fullName": "John Updated",
  "phone": "0987654321",
  "secretCode": "123456"
}
```

---

## Shopping Cart

### Get Cart

Retrieve the current user's shopping cart.

```
GET /api/cart
Authorization: Bearer <token>
```

### Add Item to Cart

Add a product to the cart.

```
POST /api/cart/items
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440001",
  "quantity": 2
}
```

### Update Cart Item Quantity

```
PUT /api/cart/items/{itemId}
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "quantity": 3
}
```

### Remove Item from Cart

```
DELETE /api/cart/items/{itemId}
Authorization: Bearer <token>
```

### Clear Cart

Remove all items from the cart.

```
DELETE /api/cart
Authorization: Bearer <token>
```

---

## Orders

### Create Order

Create a new order from the current user's shopping cart.

```
POST /api/orders
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "addressId": "550e8400-e29b-41d4-a716-446655440020",
  "shippingFee": 15000
}
```

### Get My Orders

Retrieve the current user's order history with cursor-based pagination.

```
GET /api/orders
Authorization: Bearer <token>
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `cursor` | String | No | Cursor for pagination |
| `pageSize` | int | No | Items per page (default: 20) |

### Get Order by ID

```
GET /api/orders/{orderId}
Authorization: Bearer <token>
```

### Cancel Order

```
PUT /api/orders/{orderId}/cancel
Authorization: Bearer <token>
```

---

## Reviews

### Create Review

Create a product review with optional image upload (multipart/form-data).

```
POST /api/reviews
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

**Form Data:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `request` | JSON | Yes | Review data (productId, rating, title, comment) |
| `image` | File | No | Review image (JPEG, PNG) |

**Request Example:**
```
--boundary
Content-Disposition: form-data; name="request"
Content-Type: application/json

{
  "productId": "550e8400-e29b-41d4-a716-446655440001",
  "rating": 5,
  "title": "Amazing gift!",
  "comment": "The chocolate was fresh and the packaging was beautiful."
}
--boundary
Content-Disposition: form-data; name="image"; filename="review.jpg"
Content-Type: image/jpeg

<binary data>
--boundary--
```

### Get Reviews by Product

```
GET /api/reviews/product/{productId}
```

### Get Paginated Reviews by Product

```
GET /api/reviews/product/{productId}/paginated
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `rating` | Short | No | Filter by rating (1-5) |
| `page` | int | No | Page number (default: 0) |
| `size` | int | No | Page size (default: 20) |
| `sort` | String | No | Sort field (default: createdAt) |

### Get My Reviews

```
GET /api/reviews/me
Authorization: Bearer <token>
```

### Get Review by ID

```
GET /api/reviews/{reviewId}
```

### Delete My Review

```
DELETE /api/reviews/{reviewId}
Authorization: Bearer <token>
```

---

## Gift Boxes

### Create Gift Box

Create a custom gift box with selected products.

```
POST /api/gift-boxes
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "name": "Birthday Surprise",
  "description": "A special birthday gift box",
  "boxProductId": "550e8400-e29b-41d4-a716-446655440030",
  "wrappingProductId": "550e8400-e29b-41d4-a716-446655440031",
  "ribbonProductId": "550e8400-e29b-41d4-a716-446655440032",
  "cardProductId": "550e8400-e29b-41d4-a716-446655440033",
  "greetingMessage": "Happy Birthday!",
  "items": [
    { "productId": "550e8400-e29b-41d4-a716-446655440001", "quantity": 2 },
    { "productId": "550e8400-e29b-41d4-a716-446655440002", "quantity": 1 }
  ]
}
```

### Get My Gift Boxes

```
GET /api/gift-boxes
Authorization: Bearer <token>
```

### Get Gift Box by ID

```
GET /api/gift-boxes/{id}
Authorization: Bearer <token>
```

### Delete Gift Box

```
DELETE /api/gift-boxes/{id}
Authorization: Bearer <token>
```

---

## Admin — Products

> All admin endpoints require `ADMIN` role.

### List All Products

```
GET /api/admin/products
Authorization: Bearer <admin-token>
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `categoryId` | UUID | No | Filter by category |
| `status` | String | No | Filter by status (AVAILABLE, OUT_OF_STOCK, DISCONTINUED) |
| `type` | String | No | Filter by type (COMPONENT, GIFT_BOX) |
| `page` | int | No | Page number |
| `size` | int | No | Page size (default: 20) |

### Get Product by ID

```
GET /api/admin/products/{productId}
Authorization: Bearer <admin-token>
```

### Create Product

```
POST /api/admin/products
Authorization: Bearer <admin-token>
```

**Request Body:**
```json
{
  "name": "Premium Chocolate Bar",
  "description": "Dark chocolate with hazelnuts",
  "price": 15000,
  "stockQuantity": 100,
  "categoryId": "550e8400-e29b-41d4-a716-446655440010",
  "productType": "COMPONENT",
  "status": "AVAILABLE"
}
```

### Update Product

```
PUT /api/admin/products/{productId}
Authorization: Bearer <admin-token>
```

### Delete Product

```
DELETE /api/admin/products/{productId}
Authorization: Bearer <admin-token>
```

---

## Admin — Categories

### List All Categories

```
GET /api/admin/categories
Authorization: Bearer <admin-token>
```

### Get Category by ID

```
GET /api/admin/categories/{categoryId}
Authorization: Bearer <admin-token>
```

### Create Category

```
POST /api/admin/categories
Authorization: Bearer <admin-token>
```

**Request Body:**
```json
{
  "name": "Sweets",
  "description": "Sweet treats and chocolates"
}
```

### Update Category

```
PUT /api/admin/categories/{categoryId}
Authorization: Bearer <admin-token>
```

### Delete Category

```
DELETE /api/admin/categories/{categoryId}
Authorization: Bearer <admin-token>
```

---

## Admin — Orders

### List All Orders

```
GET /api/admin/orders
Authorization: Bearer <admin-token>
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | String | No | Filter by order status |
| `userId` | UUID | No | Filter by user |

### Get Order by ID

```
GET /api/admin/orders/{orderId}
Authorization: Bearer <admin-token>
```

### Get Order by Order Number

```
GET /api/admin/orders/number/{orderNumber}
Authorization: Bearer <admin-token>
```

### Update Order Status

```
PATCH /api/admin/orders/{orderId}/status
Authorization: Bearer <admin-token>
```

**Request Body:**
```json
{
  "status": "SHIPPED"
}
```

**Available Statuses:** `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

---

## Admin — Users

### List All Users

```
GET /api/admin/users
Authorization: Bearer <admin-token>
```

### Get User by ID

```
GET /api/admin/users/{userId}
Authorization: Bearer <admin-token>
```

### Get User Roles

```
GET /api/admin/users/{userId}/roles
Authorization: Bearer <admin-token>
```

### Update User Status

```
PATCH /api/admin/users/{userId}/status
Authorization: Bearer <admin-token>
```

**Request Body:**
```json
{
  "status": "SUSPENDED"
}
```

### Assign Role to User

```
POST /api/admin/users/roles/assign
Authorization: Bearer <admin-token>
```

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "roleName": "ROLE_ADMIN"
}
```

### Remove Role from User

```
DELETE /api/admin/users/roles/remove?userId=...&roleName=ROLE_ADMIN
Authorization: Bearer <admin-token>
```

### Delete User

```
DELETE /api/admin/users/{userId}
Authorization: Bearer <admin-token>
```

---

## Admin — Reviews

### List All Reviews

```
GET /api/admin/reviews
Authorization: Bearer <admin-token>
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `productId` | UUID | No | Filter by product |
| `userId` | UUID | No | Filter by user |
| `status` | String | No | Filter by status (PENDING, APPROVED, REJECTED) |

### Get Review by ID

```
GET /api/admin/reviews/{reviewId}
Authorization: Bearer <admin-token>
```

### Update Review Status (Approve/Reject)

```
PATCH /api/admin/reviews/{reviewId}/status?status=APPROVED
Authorization: Bearer <admin-token>
```

### Delete Review

```
DELETE /api/admin/reviews/{reviewId}
Authorization: Bearer <admin-token>
```

---

## Admin — Dashboard

### Get Dashboard Statistics

Retrieve key metrics for the admin dashboard.

```
GET /api/admin/dashboard/stats
Authorization: Bearer <admin-token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalUsers": 150,
    "totalOrders": 320,
    "totalRevenue": 125000000,
    "pendingOrders": 15,
    "pendingReviews": 8,
    "totalProducts": 45
  }
}
```

---

## Common Response Format

All API responses follow a consistent format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Error Response

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: email is required",
  "path": "/api/auth/login"
}
```

### HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200` | Success |
| `201` | Created |
| `400` | Bad Request (validation error) |
| `401` | Unauthorized (missing/invalid token) |
| `403` | Forbidden (insufficient permissions) |
| `404` | Not Found |
| `409` | Conflict (e.g., duplicate email) |
| `500` | Internal Server Error |
