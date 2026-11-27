package elin.le.giftly.review.migration;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewImageStorageMigrationTest {

    @Test
    void migratesLegacyUrlAndDropsDuplicateColumn() throws Exception {
        String databaseName = "review_image_storage_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE review_image (
                            id UUID PRIMARY KEY,
                            image_url VARCHAR(500) NOT NULL,
                            secure_url VARCHAR(500) NOT NULL DEFAULT ''
                        )
                        """);
                statement.execute("INSERT INTO review_image (id, image_url) VALUES " +
                        "('" + UUID.randomUUID() + "', 'https://legacy.example.test/image.jpg')");
            }

            String migration;
            try (var input = getClass().getResourceAsStream(
                    "/db/migration/V20__simplify_review_image_storage.sql")) {
                if (input == null) {
                    throw new IllegalStateException("Review image storage migration resource not found");
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

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT secure_url FROM review_image")) {
                resultSet.next();
                assertEquals("https://legacy.example.test/image.jpg", resultSet.getString("secure_url"));
            }
            try (Statement statement = connection.createStatement()) {
                assertThrows(SQLException.class, () -> statement.executeQuery("SELECT image_url FROM review_image"));
            }
        }
    }
}