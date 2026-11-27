package elin.le.giftly.order.migration;

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

class OrderItemTypeConstraintMigrationTest {

    @Test
    void acceptsProductVariantOrderItems() throws Exception {
        String databaseName = "order_item_type_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE order_item (
                            id UUID PRIMARY KEY,
                            product_id UUID,
                            gift_box_config_id UUID,
                            product_variant_id UUID,
                            CONSTRAINT chk_order_item_type CHECK (
                                (product_id IS NOT NULL AND gift_box_config_id IS NULL) OR
                                (product_id IS NULL AND gift_box_config_id IS NOT NULL)
                            )
                        )
                        """);
            }

            assertThrows(SQLException.class,
                    () -> insertOrderItem(connection, UUID.randomUUID(), null, UUID.randomUUID()));

            String migration;
            try (var input = getClass().getResourceAsStream(
                    "/db/migration/V17__fix_order_item_type_constraint.sql")) {
                if (input == null) {
                    throw new IllegalStateException("Order item type migration resource not found");
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

            assertDoesNotThrow(() -> insertOrderItem(connection, UUID.randomUUID(), null, UUID.randomUUID()));
            assertDoesNotThrow(() -> insertOrderItem(connection, UUID.randomUUID(), UUID.randomUUID(), null));
            assertThrows(SQLException.class, () -> insertOrderItem(connection, UUID.randomUUID(), null, null));
        }
    }

    private void insertOrderItem(Connection connection, UUID id, UUID giftBoxConfigId, UUID productVariantId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO order_item (id, gift_box_config_id, product_variant_id) VALUES (?, ?, ?)")) {
            statement.setObject(1, id);
            statement.setObject(2, giftBoxConfigId);
            statement.setObject(3, productVariantId);
            statement.executeUpdate();
        }
    }
}