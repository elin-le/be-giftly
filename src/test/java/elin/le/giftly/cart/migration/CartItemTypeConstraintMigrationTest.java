package elin.le.giftly.cart.migration;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CartItemTypeConstraintMigrationTest {

    @Test
    void replacesLegacyProductIdCheckWithVariantIdCheck() throws Exception {
        String databaseName = "cart_item_type_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE cart_item (
                            id UUID PRIMARY KEY,
                            product_id UUID,
                            gift_box_config_id UUID,
                            product_variant_id UUID,
                            quantity INTEGER NOT NULL,
                            CONSTRAINT chk_cart_item_type CHECK (
                                (product_id IS NOT NULL AND gift_box_config_id IS NULL) OR
                                (product_id IS NULL AND gift_box_config_id IS NOT NULL)
                            )
                        )
                        """);
            }

            assertThrows(SQLException.class, () -> insertCartItem(
                    connection, UUID.randomUUID(), null, null, UUID.randomUUID()));

            String migration;
            try (var input = getClass().getResourceAsStream(
                    "/db/migration/V15__fix_cart_item_type_constraint.sql")) {
                if (input == null) {
                    throw new IllegalStateException("Cart item migration resource not found");
                }
                migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            try (Statement statement = connection.createStatement()) {
                for (String sql : migration.split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                    }
                }
            }

            assertDoesNotThrow(() -> insertCartItem(
                    connection, UUID.randomUUID(), null, null, UUID.randomUUID()));
            assertDoesNotThrow(() -> insertCartItem(
                    connection, UUID.randomUUID(), null, UUID.randomUUID(), null));
            assertThrows(SQLException.class, () -> insertCartItem(
                    connection, UUID.randomUUID(), null, null, null));
        }
    }

    private void insertCartItem(
            Connection connection,
            UUID id,
            UUID productId,
            UUID giftBoxConfigId,
            UUID productVariantId
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO cart_item (id, product_id, gift_box_config_id, product_variant_id, quantity) " +
                        "VALUES (?, ?, ?, ?, 1)")) {
            statement.setObject(1, id);
            statement.setObject(2, productId);
            statement.setObject(3, giftBoxConfigId);
            statement.setObject(4, productVariantId);
            statement.executeUpdate();
        }
    }
}