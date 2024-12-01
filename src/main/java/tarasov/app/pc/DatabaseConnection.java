package tarasov.app.pc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private final String LOGIN = "root";
    private final String PASS = "";
    private final String DBNAME = "pc_club";
    private final String IP = "localhost";

    public Connection connect() {
        String url = "jdbc:mysql://" + IP + ":3306/" + DBNAME + "?useSSL=false";
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Загрузка драйвера
            conn = DriverManager.getConnection(url, LOGIN, PASS);
            System.out.println("Подключение к базе данных успешно выполнено.");
        } catch (ClassNotFoundException e) {
            System.err.println("Драйвер базы данных не найден: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
        return conn;
    }

    public void disconnect(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Соединение с базой данных закрыто.");
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии соединения с базой данных: " + e.getMessage());
            }
        }
    }
}
