package tarasov.app.pc;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Logout {

    public static void performLogout(Stage currentStage) {
        try {
            // Загрузка сцены логина
            FXMLLoader loader = new FXMLLoader(Logout.class.getResource("login.fxml"));
            Parent root = loader.load();

            // Создаем новую сцену для логина
            Scene loginScene = new Scene(root, 600, 400);

            // Устанавливаем сцену на текущий stage
            currentStage.setScene(loginScene);

            // Обновляем размеры окна
            currentStage.setWidth(600);
            currentStage.setHeight(400);

            // Центрируем окно
            currentStage.centerOnScreen();

            // Устанавливаем иконку
            currentStage.getIcons().add(new Image(Objects.requireNonNull(Logout.class.getResourceAsStream("logo.png"))));

            // Устанавливаем заголовок
            currentStage.setTitle("Вход в систему");

            // Делаем окно масштабируемым (если это важно)
            currentStage.setResizable(true);

        } catch (IOException e) {
            System.err.println("Failed to load login.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
