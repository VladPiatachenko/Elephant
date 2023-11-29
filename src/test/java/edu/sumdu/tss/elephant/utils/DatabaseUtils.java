package edu.sumdu.tss.elephant.utils;
import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.sql.ScriptReader;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.Preset;

import java.io.StringReader;
import java.sql.*;

public final class DatabaseUtils {
    private DatabaseUtils() { }

    public static Database createDatabase(final User user) {
        String dbName = Preset.DB_NAME.get();
        DatabaseService.create(dbName, user.getUsername(), user.getUsername());
        return DatabaseService.byName(dbName);
    }

    public static void runSQL(final Database database, final String sql) throws SQLException {
        ScriptReader scriptReader = new ScriptReader(new StringReader(sql));
        try (Connection connection = DBPool.getConnection(database.getName()).open().getJdbcConnection()) {
            Statement statement = connection.createStatement();
            String line;

            while ((line = scriptReader.readStatement()) != null) {
                statement.executeUpdate(line);
            }
        }
    }

    public static void runSQL(final String sql) throws SQLException {
        ScriptReader scriptReader = new ScriptReader(new StringReader(sql));
        try (Connection connection = DBPool.getConnection().open().getJdbcConnection()) {
            Statement statement = connection.createStatement();
            String line;

            while ((line = scriptReader.readStatement()) != null) {
                statement.executeUpdate(line);
            }
        }
    }

    public static ResultSet selectFromDatabase(final String query) {
        try (Connection connection = DBPool.getConnection().open().getJdbcConnection()) {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static ResultSet selectFromDatabase(final Database database, final String query) {
        try (Connection connection = DBPool.getConnection(database.getName()).open().getJdbcConnection()) {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static int getResultSetSize(final ResultSet resultSet) {
        if (resultSet == null) {
            return 0;
        }
        try {
            int size = 0;
            while (resultSet.next()) {
                size++;
            }
            return size;
        } catch (SQLException ignored) {
        }
        return 0;
    }

    public static boolean testConnection(final String host, final int port, final String databaseName, final String username, final String password) {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + databaseName + "?", username, password)) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public static boolean testConnection(final String url) {
        try (Connection connection = DriverManager.getConnection(url)) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public static boolean testConnectionsMaxCount(final int maxConnectionsCount, final String url) {
        int counter = 0;
        for (int i = 0; i <= maxConnectionsCount; ++i) {
            try {
                DriverManager.getConnection(url);
                ++counter;
            } catch (SQLException ignored) {
            }
        }
        return counter == maxConnectionsCount;
    }

    public static boolean testConnectionsMaxCount(final int maxConnectionsCount, final String host, final int port, final String databaseName, final String username, final String password) {
        int counter = 0;
        for (int i = 0; i <= maxConnectionsCount; ++i) {
            try {
                DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + databaseName + "?", username, password);
                ++counter;
            } catch (SQLException ignored) {
            }
        }
        return counter == maxConnectionsCount;
    }
}