package elin.le.giftly.cart.migration;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeparateCustomGiftBoxFromCartMigrationTest {

    @Test
    void removesCustomBoxCartLinksButKeepsConfigurationsAndRequiresVariants() throws Exception {
        String databaseName = "cart_custom_box_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL")) {
            UUID configId = UUID.randomUUID();
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE gift_box_config (id UUID PRIMARY KEY)");
                statement.execute("""
                        CREATE TABLE cart_item (
                            id UUID PRIMARY KEY,
                            cart_id UUID NOT NULL,
                            product_id UUID,
                            product_variant_id UUID,
                            gift_box_config_id UUID,
                            quantity INTEGER NOT NULL,
                            CONSTRAINT fk_cart_item_gift_box FOREIGN KEY (gift_box_config_id)
                                REFERENCES gift_box_config(id),
                            CONSTRAINT chk_cart_item_type CHECK (
                                (product_variant_id IS NOT NULL AND gift_box_config_id IS NULL) OR
                                (product_variant_id IS NULL AND gift_box_config_id IS NOT NULL)
                            )
                        )
                        """);
                statement.execute("INSERT INTO gift_box_config (id) VALUES ('" + configId + "')");
            }
            insertCustomCartItem(connection, UUID.randomUUID(), configId);

            String migration;
            try (var input = getClass().getResourceAsStream(
                    "/db/migration/V22__separate_custom_gift_boxes_from_cart.sql")) {
                if (input == null) {
                    throw new IllegalStateException("Cart and custom gift box migration resource not found");
                }
                migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(migration);
            }

            assertEquals(0, countRows(connection, "cart_item"));
            assertEquals(1, countRows(connection, "gift_box_config"));
            assertDoesNotThrow(() -> insertVariantCartItem(connection));
            assertThrows(SQLException.class, () -> insertCartItemWithoutVariant(connection));
        }
    }

    private void insertCustomCartItem(Connection connection, UUID cartId, UUID configId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO cart_item (id, cart_id, gift_box_config_id, quantity)
                VALUES (?, ?, ?, 1)
                """)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, cartId);
            statement.setObject(3, configId);
            statement.executeUpdate();
        }
    }

    private void insertVariantCartItem(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO cart_item (id, cart_id, product_variant_id, quantity)
                VALUES (?, ?, ?, 1)
                """)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, UUID.randomUUID());
            statement.setObject(3, UUID.randomUUID());
            statement.executeUpdate();
        }
    }

    private void insertCartItemWithoutVariant(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO cart_item (id, cart_id, quantity)
                VALUES (?, ?, 1)
                """)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, UUID.randomUUID());
            statement.executeUpdate();
        }
    }

    private int countRows(Connection connection, String tableName) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}