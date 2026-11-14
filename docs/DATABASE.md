# Database Schema

## Entity-Relationship Diagram

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    user      │──┐    │  user_role   │──┐    │     role     │
└──────┬───────┘  │    └──────────────┘  │    └──────────────┘
       │          │                       │
       │ 1        │ 1                     │ 1
       │   ┌──────┴───┐              ┌────┴───┐
       │   │  address  │              │ product│
       │   └──────┬───┘              └───┬────┘
       │ 1        │ 1                    │ 1
       │   ┌──────┴───┐              ┌───┴──────────┐
       │   │   cart    │              │  category    │
       │   └──────┬───┘              └──────────────┘
       │ 1        │ 1
       │   ┌──────┴───┐
       │   │ cart_item │
       │   └──────┬───┘
       │ 1        │ *
       │   ┌──────┴─────────────────────────────────┐
       │   │                                         │
       ▼   ▼                                         ▼
┌──────────────┐  ┌──────────────┐       ┌──────────────────┐
│    order     │  │gift_box_config│       │  product_image   │
└──────┬───────┘  └───────┬──────┘       └──────────────────┘
       │ 1                │ 1
       │   ┌──────────────┴──┐
       │   │  order_item     │
       │   └────────┬────────┘
       │ 1          │ 1
       │   ┌────────┴────────┐       ┌──────────────────┐
       │   │order_item_      │       │    payment       │
       │   │  component      │       └──────────────────┘
       │   └─────────────────┘
       │ 1
       │   ┌──────────────────┐
       └──►│     review       │
           └──────────────────┘
```

## Tables

### 1. User & Role Management

#### `user`

Stores user accounts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique user identifier |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | User email (login credential) |
| `password` | VARCHAR(255) | NOT NULL | BCrypt-hashed password |
| `full_name` | VARCHAR(255) | NOT NULL | User's full name |
| `phone` | VARCHAR(50) | NULL | Phone number |
| `status` | VARCHAR(20) | NOT NULL | User status (ACTIVE, SUSPENDED, BANNED) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Account creation time |
| `updated_at` | TIMESTAMP | NULL | Last update time |

**Indexes:**
- `idx_user_email` on `email`

---

#### `role`

Defines available roles.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique role identifier |
| `name` | VARCHAR(50) | NOT NULL, UNIQUE | Role name (e.g., ROLE_USER, ROLE_ADMIN) |

**Default Data:**
- `ROLE_USER` — Standard customer
- `ROLE_ADMIN` — Administrator

---

#### `user_role`

Many-to-many relationship between users and roles.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique identifier |
| `user_id` | UUID | NOT NULL, FK → user(id) | User reference |
| `role_id` | UUID | NOT NULL, FK → role(id) | Role reference |

**Constraints:**
- `uk_user_role` — UNIQUE (user_id, role_id) — prevents duplicate role assignments

---

#### `address`

Stores user shipping addresses.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique address identifier |
| `user_id` | UUID | NOT NULL, FK → user(id) | Owner user |
| `recipient_name` | VARCHAR(255) | NOT NULL | Name of recipient |
| `phone` | VARCHAR(50) | NOT NULL | Contact phone |
| `address_line` | VARCHAR(500) | NOT NULL | Street address |
| `ward` | VARCHAR(100) | NULL | Ward/sub-district |
| `district` | VARCHAR(100) | NULL | District |
| `city` | VARCHAR(100) | NULL | City/province |
| `is_default` | BOOLEAN | DEFAULT FALSE | Default address flag |

**Indexes:**
- `idx_address_user` on `user_id`

---

### 2. Product Management

#### `category`

Product categories.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique category identifier |
| `name` | VARCHAR(255) | NOT NULL | Category name |
| `description` | TEXT | NULL | Category description |

---

#### `product`

Products in the catalog.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique product identifier |
| `category_id` | UUID | NOT NULL, FK → category(id) | Product category |
| `name` | VARCHAR(255) | NOT NULL | Product name |
| `description` | TEXT | NULL | Product description |
| `price` | NUMERIC(19,2) | NOT NULL | Product price |
| `stock_quantity` | INTEGER | NOT NULL, DEFAULT 0 | Available stock |
| `product_type` | VARCHAR(20) | NOT NULL | Type: COMPONENT or GIFT_BOX |
| `status` | VARCHAR(20) | NOT NULL | Status: ACTIVE, INACTIVE, OUT_OF_STOCK |

**Indexes:**
- `idx_product_category` on `category_id`
- `idx_product_status` on `status`
- `idx_product_type` on `product_type`
- `idx_product_name_trgm` — GIN trigram index on `name` (fuzzy search)
- `idx_product_description_trgm` — GIN trigram index on `description` (fuzzy search)

---

#### `product_image`

Product images stored on Cloudinary.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique image identifier |
| `product_id` | UUID | NOT NULL, FK → product(id) | Parent product |
| `image_url` | VARCHAR(500) | NOT NULL | Public image URL |
| `secure_url` | VARCHAR(500) | NOT NULL | HTTPS image URL |
| `public_id` | VARCHAR(255) | NOT NULL | Cloudinary public ID |
| `is_thumbnail` | BOOLEAN | DEFAULT FALSE | Thumbnail flag |

**Indexes:**
- `idx_product_image_product` on `product_id`

---

### 3. Gift Box Customization

#### `gift_box_config`

Custom gift box configurations created by users.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique config identifier |
| `user_id` | UUID | NOT NULL, FK → user(id) | Creating user |
| `box_product_id` | UUID | NOT NULL, FK → product(id) | Box container product |
| `wrapping_product_id` | UUID | NULL, FK → product(id) | Wrapping paper product |
| `ribbon_product_id` | UUID | NULL, FK → product(id) | Ribbon product |
| `card_product_id` | UUID | NULL, FK → product(id) | Greeting card product |
| `greeting_message` | TEXT | NULL | Custom greeting text |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |

**Indexes:**
- `idx_gift_box_config_user` on `user_id`

---

#### `gift_box_item`

Items inside a gift box configuration.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique item identifier |
| `config_id` | UUID | NOT NULL, FK → gift_box_config(id) | Parent config |
| `product_id` | UUID | NOT NULL, FK → product(id) | Product in the box |
| `quantity` | INTEGER | NOT NULL | Quantity of this product |

**Indexes:**
- `idx_gift_box_item_config` on `config_id`

---

### 4. Shopping Cart

#### `cart`

User shopping carts (one per user).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique cart identifier |
| `user_id` | UUID | NOT NULL, FK → user(id) | Owner user |
| `updated_at` | TIMESTAMP | NULL | Last update time |

**Indexes:**
- `idx_cart_user` on `user_id`

---

#### `cart_item`

Items in a shopping cart.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique item identifier |
| `cart_id` | UUID | NOT NULL, FK → cart(id) | Parent cart |
| `product_id` | UUID | NULL, FK → product(id) | Regular product (mutually exclusive with gift_box_config_id) |
| `gift_box_config_id` | UUID | NULL, FK → gift_box_config(id) | Gift box config (mutually exclusive with product_id) |
| `quantity` | INTEGER | NOT NULL | Item quantity |

**Constraints:**
- `chk_cart_item_type` — CHECK: exactly one of `product_id` or `gift_box_config_id` must be set

**Indexes:**
- `idx_cart_item_cart` on `cart_id`

---

### 5. Order Management

#### `order`

Customer orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique order identifier |
| `user_id` | UUID | NOT NULL, FK → user(id) | Ordering user |
| `address_id` | UUID | NOT NULL, FK → address(id) | Shipping address |
| `order_number` | VARCHAR(50) | NOT NULL, UNIQUE | Human-readable order number |
| `subtotal` | NUMERIC(19,2) | NOT NULL | Sum of item prices |
| `shipping_fee` | NUMERIC(19,2) | NOT NULL | Shipping cost |
| `total_amount` | NUMERIC(19,2) | NOT NULL | subtotal + shipping_fee |
| `status` | VARCHAR(20) | NOT NULL | Order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED) |
| `ordered_at` | TIMESTAMP | NOT NULL | Order placement time |

**Indexes:**
- `idx_order_user` on `user_id`
- `idx_order_number` on `order_number`
- `idx_order_status` on `status`

---

#### `order_item`

Line items in an order.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique item identifier |
| `order_id` | UUID | NOT NULL, FK → order(id) | Parent order |
| `product_id` | UUID | NULL, FK → product(id) | Regular product (mutually exclusive with gift_box_config_id) |
| `gift_box_config_id` | UUID | NULL, FK → gift_box_config(id) | Gift box config (mutually exclusive with product_id) |
| `item_name` | VARCHAR(255) | NOT NULL | Snapshot of item name |
| `unit_price` | NUMERIC(19,2) | NOT NULL | Price at time of order |
| `quantity` | INTEGER | NOT NULL | Quantity ordered |
| `subtotal` | NUMERIC(19,2) | NOT NULL | unit_price × quantity |

**Constraints:**
- `chk_order_item_type` — CHECK: exactly one of `product_id` or `gift_box_config_id` must be set

**Indexes:**
- `idx_order_item_order` on `order_id`

---

#### `order_item_component`

Components of a gift box order item (box, wrapping, ribbon, card, contents).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique component identifier |
| `order_item_id` | UUID | NOT NULL, FK → order_item(id) | Parent order item |
| `component_name` | VARCHAR(255) | NOT NULL | Component name |
| `component_type` | VARCHAR(50) | NOT NULL | Type (BOX, WRAPPING, RIBBON, CARD, CONTENT) |
| `unit_price` | NUMERIC(19,2) | NOT NULL | Component price |
| `quantity` | INTEGER | NOT NULL | Component quantity |

**Indexes:**
- `idx_order_item_component_order_item` on `order_item_id`

---

### 6. Payment

#### `payment`

Payment records for orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique payment identifier |
| `order_id` | UUID | NOT NULL, FK → order(id) | Related order |
| `payment_method` | VARCHAR(20) | NOT NULL | Method (CASH_ON_DELIVERY, BANK_TRANSFER, etc.) |
| `payment_status` | VARCHAR(20) | NOT NULL | Status (PENDING, COMPLETED, FAILED, REFUNDED) |
| `amount` | NUMERIC(19,2) | NOT NULL | Payment amount |
| `paid_at` | TIMESTAMP | NULL | Payment completion time |

**Indexes:**
- `idx_payment_order` on `order_id`

---

### 7. Reviews

#### `review`

Product reviews by users.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | Unique review identifier |
| `user_id` | UUID | NOT NULL, FK → user(id) | Reviewing user |
| `product_id` | UUID | NOT NULL, FK → product(id) | Reviewed product |
| `order_item_id` | UUID | NULL, FK → order_item(id) | Related order item (one review per item) |
| `rating` | INTEGER | NOT NULL | Rating (1-5) |
| `comment` | TEXT | NULL | Review text |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Review creation time |

**Constraints:**
- `chk_review_rating` — CHECK: rating BETWEEN 1 AND 5
- `uk_review_order_item` — UNIQUE (order_item_id) — one review per order item

**Indexes:**
- `idx_review_product` on `product_id`
- `idx_review_user` on `user_id`

---

## Enum Values

### User Status (`user.status`)

| Value | Description |
|-------|-------------|
| `ACTIVE` | Normal active user |
| `SUSPENDED` | Temporarily suspended |
| `BANNED` | Permanently banned |

### Product Type (`product.product_type`)

| Value | Description |
|-------|-------------|
| `COMPONENT` | Individual product (chocolate, wrapping, etc.) |
| `GIFT_BOX` | Pre-made gift box product |

### Product Status (`product.status`)

| Value | Description |
|-------|-------------|
| `AVAILABLE` | Available for purchase |
| `OUT_OF_STOCK` | Temporarily unavailable |
| `DISCONTINUED` | No longer sold |

### Order Status (`order.status`)

| Value | Description |
|-------|-------------|
| `PENDING` | Order placed, awaiting processing |
| `PROCESSING` | Being prepared |
| `SHIPPED` | Dispatched to customer |
| `DELIVERED` | Received by customer |
| `CANCELLED` | Order cancelled |

### Payment Method (`payment.payment_method`)

| Value | Description |
|-------|-------------|
| `CASH_ON_DELIVERY` | Pay on delivery |
| `BANK_TRANSFER` | Bank transfer |

### Payment Status (`payment.payment_status`)

| Value | Description |
|-------|-------------|
| `PENDING` | Awaiting payment |
| `COMPLETED` | Payment received |
| `FAILED` | Payment failed |
| `REFUNDED` | Payment refunded |

---

## Database Migrations

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__init_schema.sql` | Initial schema: all 14 tables, foreign keys, constraints, indexes, default roles |
| V2 | `V2__enable_pg_trgm.sql` | Enable pg_trgm extension; create GIN trigram indexes on product name and description |
| V3 | `V3__add_review_constraint.sql` | Add unique constraint on review.order_item_id (one review per order item) |

---

## Key Relationships

```
user (1) ──── (N) address
user (1) ──── (N) cart
user (1) ──── (1) cart ──── (N) cart_item
user (1) ──── (N) order
user (1) ──── (N) review
user (N) ──── (N) role  (via user_role)

order (1) ──── (N) order_item
order (1) ──── (1) payment
order_item (1) ──── (N) order_item_component

category (1) ──── (N) product
product (1) ──── (N) product_image
product (1) ──── (N) review

gift_box_config (1) ──── (N) gift_box_item
gift_box_config (1) ──── (N) cart_item  (optional)
gift_box_config (1) ──── (N) order_item (optional)
```
