package elin.le.giftly.giftbox.migration;

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

class GiftBoxLegacyProductColumnMigrationTest {

    @Test
    void allowsCreatingGiftBoxConfigWithoutLegacyBoxProduct() throws Exception {
        String databaseName = "gift_box_legacy_product_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE gift_box_config ("
                        + "id UUID PRIMARY KEY, user_id UUID NOT NULL, box_product_id UUID NOT NULL)");
            }

            UUID configId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            assertThrows(SQLException.class, () -> insertConfig(connection, configId, userId));

            runMigration(connection);

            assertDoesNotThrow(() -> insertConfig(connection, configId, userId));
        }
    }

    private void insertConfig(Connection connection, UUID configId, UUID userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO gift_box_config (id, user_id) VALUES (?, ?)")) {
            statement.setObject(1, configId);
            statement.setObject(2, userId);
            statement.executeUpdate();
        }
    }

    private void runMigration(Connection connection) throws Exception {
        String migration;
        try (var input = getClass().getResourceAsStream(
                "/db/migration/V23__make_legacy_gift_box_product_nullable.sql")) {
            if (input == null) {
                throw new IllegalStateException("Legacy gift box product migration resource not found");
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
    }
}