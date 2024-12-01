package tarasov.app.pc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddGameDialogController {

    @FXML
    private TextField gameNameField;
    @FXML
    private TextArea gameDescriptionArea;
    @FXML
    private ComboBox<String> genreComboBox;

    private final DatabaseConnection databaseConnection = new DatabaseConnection();

    private ObservableList<String> genres = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadGenres();
    }

    private void loadGenres() {
        String query = "SELECT name FROM Genres";
        try (Connection conn = databaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                genres.add(rs.getString("name"));
            }
            genreComboBox.setItems(genres);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        String name = gameNameField.getText().trim();
        String description = gameDescriptionArea.getText().trim();
        String genre = genreComboBox.getValue();

        if (name.isEmpty() || description.isEmpty() || genre == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Пожалуйста, заполните все поля.");
            alert.showAndWait();
            return;
        }

        String query = "INSERT INTO Games (game_name, description, genre_id) VALUES (?, ?, " +
                "(SELECT id FROM Genres WHERE name = ?))";

        try (Connection conn = databaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, genre);
            stmt.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Игра успешно добавлена!");
            alert.showAndWait();
            closeDialog();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Произошла ошибка при добавлении игры.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) gameNameField.getScene().getWindow();
        stage.close();
    }
}
