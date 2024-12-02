package tarasov.app.pc;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private DatabaseConnection databaseConnection;

    private int userId = -1; // Хранение user_id
    private String userRole = null; // Хранение роли пользователя

    /*====================== ИНИЦИЛИЗАЦИЯ ==========================*/

    @FXML
    public void initialize() {
        databaseConnection = new DatabaseConnection();
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> openRegisterWindow());
    }

    private void openRegisterWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
            Parent root = loader.load();
            Stage registerStage = new Stage();
            registerStage.setScene(new Scene(root));
            registerStage.setTitle("Регистрация");
            registerStage.show();

            // Закрытие текущего окна входа
            Stage loginStage = (Stage) registerButton.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            AlertManager.showErrorAlert("Ошибка открытия окна регистрации", "Не удалось открыть окно регистрации.");
            e.printStackTrace();
        }
    }

    // Метод для обработки кнопки входа
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertManager.showWarningAlert("Поля пусты", "Пожалуйста, заполните все поля.");
            return;
        }

        // Получаем user_id если логин и пароль верны
        userId = validateLogin(username, password);

        if (userId != -1) {

            if (Objects.equals(userRole, "client")) {
                // Передача user_id в MainPageController
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("mainPage.fxml"));
                    Parent root = loader.load();

                    // Получаем контроллер главной страницы
                    MainPageController mainPageController = loader.getController();
                    mainPageController.setUserId(userId, userRole); // Передаем user_id

                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("css/mainPage.css").toExternalForm());
                    stage.setScene(scene);

                    // Устанавливаем размеры окна
                    stage.setWidth(1250);  // Установите нужную ширину
                    stage.setHeight(900); // Установите нужную высоту
                    stage.setResizable(false);

                    // Устанавливаем позицию окна по центру экрана
                    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                    stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - stage.getWidth()) / 2);
                    stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - stage.getHeight()) / 2);

                    // Устанавливаем заголовок окна
                    stage.setTitle("Моя страница");

                    // Показываем окно
                    stage.show();
                } catch (IOException e) {
                    AlertManager.showErrorAlert("Ошибка перехода", "Не удалось открыть основное окно.");
                    e.printStackTrace();
                }
            } else {
                // Передача user_id в MainPageController
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("adminPage.fxml"));
                    Parent root = loader.load();

                    // Получаем контроллер главной страницы
                    AdminPageController adminPageController = loader.getController();
                    adminPageController.setUserId(userId, userRole); // Передаем user_id

                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("css/mainPage.css").toExternalForm());
                    stage.setScene(scene);

                    // Устанавливаем размеры окна
                    stage.setWidth(1250);  // Установите нужную ширину
                    stage.setHeight(900); // Установите нужную высоту
                    stage.setResizable(false);

                    // Устанавливаем позицию окна по центру экрана
                    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                    stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - stage.getWidth()) / 2);
                    stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - stage.getHeight()) / 2);

                    // Устанавливаем заголовок окна
                    stage.setTitle("Моя страница");

                    // Показываем окно
                    stage.show();
                } catch (IOException e) {
                    AlertManager.showErrorAlert("Ошибка перехода", "Не удалось открыть основное окно.");
                    e.printStackTrace();
                }
            }
        } else {
            AlertManager.showErrorAlert("Ошибка входа", "Неверный логин или пароль.");
        }
    }

    // Метод для проверки логина и пароля через базу данных
    private int validateLogin(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String query = "SELECT id, role FROM Users WHERE username = ? AND password_hash = ?;";

        try {
            conn = databaseConnection.connect();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id"); // Получаем user_id
                userRole = rs.getString("role"); // Получаем роль пользователя
            }

        } catch (SQLException e) {
            AlertManager.showErrorAlert("Ошибка базы данных", "Ошибка при проверке учетных данных.");
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) databaseConnection.disconnect(conn);
            } catch (SQLException e) {
                AlertManager.showErrorAlert("Ошибка базы данных", "Ошибка при закрытии ресурсов.");
                e.printStackTrace();
            }
        }

        return userId; // Возвращаем user_id или -1
    }
}
