-- V1: Initial schema for Giftly application

-- 1. User & Role Management

CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE "role" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE "user_role" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES "user"(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES "role"(id),
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE "address" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    address_line VARCHAR(500) NOT NULL,
    ward VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);

-- 2. Product Management

CREATE TABLE "category" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE "product" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    product_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES "category"(id)
);

CREATE TABLE "product_image" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    secure_url VARCHAR(500) NOT NULL,
    public_id VARCHAR(255) NOT NULL,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES "product"(id)
);

-- 3. Gift Box Customization

CREATE TABLE "gift_box_config" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    box_product_id UUID NOT NULL,
    wrapping_product_id UUID,
    ribbon_product_id UUID,
    card_product_id UUID,
    greeting_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gift_box_config_user FOREIGN KEY (user_id) REFERENCES "user"(id),
    CONSTRAINT fk_gift_box_config_box FOREIGN KEY (box_product_id) REFERENCES "product"(id),
    CONSTRAINT fk_gift_box_config_wrapping FOREIGN KEY (wrapping_product_id) REFERENCES "product"(id),
    CONSTRAINT fk_gift_box_config_ribbon FOREIGN KEY (ribbon_product_id) REFERENCES "product"(id),
    CONSTRAINT fk_gift_box_config_card FOREIGN KEY (card_product_id) REFERENCES "product"(id)
);

CREATE TABLE "gift_box_item" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_gift_box_item_config FOREIGN KEY (config_id) REFERENCES "gift_box_config"(id),
    CONSTRAINT fk_gift_box_item_product FOREIGN KEY (product_id) REFERENCES "product"(id)
);

-- 4. Shopping Cart

CREATE TABLE "cart" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE "cart_item" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL,
    product_id UUID,
    gift_box_config_id UUID,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES "cart"(id),
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES "product"(id),
    CONSTRAINT fk_cart_item_gift_box FOREIGN KEY (gift_box_config_id) REFERENCES "gift_box_config"(id),
    CONSTRAINT chk_cart_item_type CHECK (
        (product_id IS NOT NULL AND gift_box_config_id IS NULL) OR
        (product_id IS NULL AND gift_box_config_id IS NOT NULL)
    )
);

-- 5. Order Management

CREATE TABLE "order" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    address_id UUID NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    subtotal NUMERIC(19, 2) NOT NULL,
    shipping_fee NUMERIC(19, 2) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    ordered_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES "user"(id),
    CONSTRAINT fk_order_address FOREIGN KEY (address_id) REFERENCES "address"(id)
);

CREATE TABLE "order_item" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id UUID,
    gift_box_config_id UUID,
    item_name VARCHAR(255) NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES "order"(id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES "product"(id),
    CONSTRAINT fk_order_item_gift_box FOREIGN KEY (gift_box_config_id) REFERENCES "gift_box_config"(id),
    CONSTRAINT chk_order_item_type CHECK (
        (product_id IS NOT NULL AND gift_box_config_id IS NULL) OR
        (product_id IS NULL AND gift_box_config_id IS NOT NULL)
    )
);

CREATE TABLE "order_item_component" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id UUID NOT NULL,
    component_name VARCHAR(255) NOT NULL,
    component_type VARCHAR(50) NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_order_item_component_order_item FOREIGN KEY (order_item_id) REFERENCES "order_item"(id)
);

-- 6. Payment & Review

CREATE TABLE "payment" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    paid_at TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES "order"(id)
);

CREATE TABLE "review" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    order_item_id UUID,
    rating INTEGER NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES "user"(id),
    CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES "product"(id),
    CONSTRAINT fk_review_order_item FOREIGN KEY (order_item_id) REFERENCES "order_item"(id),
    CONSTRAINT chk_review_rating CHECK (rating >= 1 AND rating <= 5)
);

-- Insert default roles
INSERT INTO "role" (id, name) VALUES (gen_random_uuid(), 'ROLE_USER');
INSERT INTO "role" (id, name) VALUES (gen_random_uuid(), 'ROLE_ADMIN');

-- Create indexes for performance
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_address_user ON "address"(user_id);
CREATE INDEX idx_product_category ON "product"(category_id);
CREATE INDEX idx_product_status ON "product"(status);
CREATE INDEX idx_product_type ON "product"(product_type);
CREATE INDEX idx_product_image_product ON "product_image"(product_id);
CREATE INDEX idx_gift_box_config_user ON "gift_box_config"(user_id);
CREATE INDEX idx_gift_box_item_config ON "gift_box_item"(config_id);
CREATE INDEX idx_cart_user ON "cart"(user_id);
CREATE INDEX idx_cart_item_cart ON "cart_item"(cart_id);
CREATE INDEX idx_order_user ON "order"(user_id);
CREATE INDEX idx_order_number ON "order"(order_number);
CREATE INDEX idx_order_status ON "order"(status);
CREATE INDEX idx_order_item_order ON "order_item"(order_id);
CREATE INDEX idx_order_item_component_order_item ON "order_item_component"(order_item_id);
CREATE INDEX idx_payment_order ON "payment"(order_id);
CREATE INDEX idx_review_product ON "review"(product_id);
CREATE INDEX idx_review_user ON "review"(user_id);
