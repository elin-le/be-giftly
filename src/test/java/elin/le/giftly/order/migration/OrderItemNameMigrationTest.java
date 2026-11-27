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

class OrderItemNameMigrationTest {

    @Test
    void allowsOrderItemsToWriteNameSnapshotWithoutLegacyName() throws Exception {
        String databaseName = "order_item_name_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE order_item (
                            id UUID PRIMARY KEY,
                            item_name VARCHAR(255) NOT NULL,
                            item_name_snapshot VARCHAR(255) NOT NULL
                        )
                        """);
            }

            assertThrows(SQLException.class, () -> insertOrderItem(connection));

            String migration;
            try (var input = getClass().getResourceAsStream(
                    "/db/migration/V16__make_legacy_order_item_name_nullable.sql")) {
                if (input == null) {
                    throw new IllegalStateException("Order item migration resource not found");
                }
                migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(migration);
            }

            assertDoesNotThrow(() -> insertOrderItem(connection));
        }
    }

    private void insertOrderItem(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO order_item (id, item_name_snapshot) VALUES (?, ?)")) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, "Test product");
            statement.executeUpdate();
        }
    }
}