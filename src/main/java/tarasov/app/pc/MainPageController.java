package tarasov.app.pc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField; // Импортируем TextField для поиска
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainPageController {

    @FXML
    private TableView<Computer> computersTable;
    @FXML
    private TableColumn<Computer, String> computerNameColumn;
    @FXML
    private TableColumn<Computer, String> specificationsColumn;

    @FXML
    private TableView<Game> gamesTable;
    @FXML
    private TableColumn<Game, String> gameNameColumn;
    @FXML
    private TableColumn<Game, String> descriptionColumn;
    @FXML
    private TableColumn<Game, String> genreColumn;

    @FXML
    private ComboBox<String> genreFilterComboBox;

    @FXML
    private TextField searchTextField; // Добавляем поле для поиска по названию игры

    private int user_id;
    private String user_role;

    private final DatabaseConnection databaseConnection = new DatabaseConnection();
    private ObservableList<Genre> genres = FXCollections.observableArrayList();
    private ObservableList<Game> allGames = FXCollections.observableArrayList(); // Список всех игр

    public void setUserId(int userId, String userRole) {
        this.user_id = userId;
        this.user_role = userRole;
    }

    @FXML
    public void initialize() {
        // Инициализация столбцов таблицы компьютеров
        computerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        specificationsColumn.setCellValueFactory(new PropertyValueFactory<>("specifications"));

        // Инициализация столбцов таблицы игр
        gameNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        // Загрузка данных
        loadComputers();
        loadGames();

        computersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // При одиночном клике
                handleComputerSelection();
            }
        });

        // Обработчик изменения жанра
        genreFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> filterGames(newValue, searchTextField.getText()));

        // Обработчик текста в поле поиска
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> filterGames(genreFilterComboBox.getValue(), newValue));
    }

    @FXML
    public void handleComputerSelection() {
        Computer selectedComputer = computersTable.getSelectionModel().getSelectedItem();
        if (selectedComputer == null) {
            AlertManager.showWarningAlert("Ошибка", "Пожалуйста, выберите компьютер.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ReserveModal.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Модальное окно
            stage.setTitle("Зарезервировать компьютер");

            // Загружаем модальное окно
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);

            // Передаем выбранный компьютер и user_id в контроллер
            ReserveModalController controller = loader.getController();
            controller.setSelectedComputer(selectedComputer, user_id);

            stage.showAndWait(); // Ожидаем закрытия модального окна

        } catch (Exception e) {
            System.err.println("Ошибка при открытии модального окна: " + e.getMessage());
            AlertManager.showErrorAlert("Ошибка", "Ошибка при открытии модального окна: " + e.getMessage());
        }
    }

    private void loadComputers() {
        ObservableList<Computer> computers = FXCollections.observableArrayList();
        String query = "SELECT computer_id, computer_name, specifications FROM Computers";

        try (Connection conn = databaseConnection.connect(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                computers.add(new Computer(rs.getInt("computer_id"), rs.getString("computer_name"), rs.getString("specifications")));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке данных о компьютерах: " + e.getMessage());
        }

        computersTable.setItems(computers);
    }

    private void loadGames() {
        String query = "SELECT games.game_id, games.game_name, games.description, genres.id AS genre_id, genres.name AS genre_name " +
                "FROM Games " +
                "JOIN Genres ON Genres.id = Games.genre_id;";

        try (Connection conn = databaseConnection.connect(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Game game = new Game(
                        rs.getInt("game_id"),
                        rs.getString("game_name"),
                        rs.getString("description"),
                        rs.getInt("genre_id"),
                        rs.getString("genre_name")
                );
                allGames.add(game);  // Добавляем игру в общий список

                // Добавляем жанр, если его ещё нет в списке
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
                if (genres.stream().noneMatch(g -> g.getId() == genre.getId())) {
                    genres.add(genre);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке данных об играх: " + e.getMessage());
        }

        gamesTable.setItems(allGames); // Устанавливаем все игры в таблицу

        // Добавляем "Все" в ComboBox
        ObservableList<String> genreNames = FXCollections.observableArrayList("Все");
        genreNames.addAll(genres.stream().map(Genre::getName).toList());
        genreFilterComboBox.setItems(genreNames);

        // Устанавливаем "Все" как значение по умолчанию
        genreFilterComboBox.setValue("Все");
    }

    private void filterGames(String genre, String searchText) {
        ObservableList<Game> filteredGames = FXCollections.observableArrayList();

        // Фильтрация по жанру
        for (Game game : allGames) {
            boolean matchesGenre = genre.equals("Все") || game.getGenre().equals(genre);
            boolean matchesSearchText = game.getName().toLowerCase().contains(searchText.toLowerCase());

            if (matchesGenre && matchesSearchText) {
                filteredGames.add(game);
            }
        }

        gamesTable.setItems(filteredGames); // Обновляем таблицу с отфильтрованными играми
    }
}
