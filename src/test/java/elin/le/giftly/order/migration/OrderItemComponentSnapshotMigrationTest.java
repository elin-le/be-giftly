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

class OrderItemComponentSnapshotMigrationTest {

    @Test
    void allowsSnapshotColumnsWithoutLegacyComponentNameAndType() throws Exception {
        String databaseName = "order_item_component_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE order_item_component (
                            id UUID PRIMARY KEY,
                            order_item_id UUID NOT NULL,
                            component_name VARCHAR(255) NOT NULL,
                            component_type VARCHAR(50) NOT NULL,
                            product_variant_id UUID,
                            product_name_snapshot VARCHAR(255),
                            sku_snapshot VARCHAR(255),
                            quantity INTEGER,
                            unit_price NUMERIC(19, 2)
                        )
                        """);
            }

            assertThrows(SQLException.class, () -> insertSnapshot(connection));

            String migration;
            try (var input = getClass().getResourceAsStream(
                    "/db/migration/V21__allow_order_item_component_snapshots.sql")) {
                if (input == null) {
                    throw new IllegalStateException("Order item component migration resource not found");
                }
                migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(migration);
            }

            assertDoesNotThrow(() -> insertSnapshot(connection));
        }
    }

    private void insertSnapshot(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO order_item_component (
                    id, order_item_id, product_variant_id, product_name_snapshot, sku_snapshot, quantity, unit_price
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, UUID.randomUUID());
            statement.setObject(3, UUID.randomUUID());
            statement.setString(4, "Gift box component");
            statement.setString(5, "COMPONENT-01");
            statement.setInt(6, 1);
            statement.setBigDecimal(7, new java.math.BigDecimal("10.00"));
            statement.executeUpdate();
        }
    }
}