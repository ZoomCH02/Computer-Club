package tarasov.app.pc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.format.DateTimeFormatter;

public class AdminPageController {

    @FXML
    private TableView<Game> gamesTable;
    @FXML
    private TableView<ReservedTime> reservationsTable;
    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Game, Integer> gameIdColumn;
    @FXML
    private TableColumn<Game, String> gameNameColumn;
    @FXML
    private TableColumn<Game, String> gameDescriptionColumn;
    @FXML
    private TableColumn<Game, String> gameGenreColumn;

    @FXML
    private TableColumn<ReservedTime, String> userIdColumn;
    @FXML
    private TableColumn<ReservedTime, String> computerIdColumn;
    @FXML
    private TableColumn<ReservedTime, String> reservationTimeColumn;
    @FXML
    private TableColumn<ReservedTime, String> endTimeColumn;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, Integer> orderUserIdColumn;
    @FXML
    private TableColumn<Order, Integer> orderComputerIdColumn;
    @FXML
    private TableColumn<Order, Timestamp> orderStartTimeColumn;
    @FXML
    private TableColumn<Order, Timestamp> orderEndTimeColumn;
    @FXML
    private TableColumn<Order, Double> totalCostColumn;

    private final DatabaseConnection databaseConnection = new DatabaseConnection();

    private int user_id;
    private String user_role;

    public void setUserId(int userId, String userRole) {
        this.user_id = userId;
        this.user_role = userRole;

        loadGames();
        loadReservations();
        loadOrders();

        gameIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        gameNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        gameDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        gameGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userProperty()); // Привязка для времени окончания
        computerIdColumn.setCellValueFactory(cellData -> cellData.getValue().pcIdProperty());
        reservationTimeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        endTimeColumn.setCellValueFactory(cellData -> cellData.getValue().endTimeProperty());

        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("OrderId"));
        orderUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("UserId"));
        orderComputerIdColumn.setCellValueFactory(new PropertyValueFactory<>("ComputerId"));
        orderStartTimeColumn.setCellValueFactory(new PropertyValueFactory<>("StartTime"));
        orderEndTimeColumn.setCellValueFactory(new PropertyValueFactory<>("EndTime"));
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("TotalCost"));

        setupContextMenuForReservations();
        setupContextMenuForGames(); // Добавляем настройку контекстного меню для игр
    }

    // Метод для инициализации данных в таблицах
    public void initialize() {

    }

    private void loadGames() {
        ObservableList<Game> games = FXCollections.observableArrayList();
        String query = "SELECT Games.game_id, Games.game_name, Games.description, Genres.id AS genre_id, Genres.name AS genre_name " +
                "FROM Games " +
                "JOIN Genres ON Genres.id = Games.genre_id;";

        try (Connection conn = databaseConnection.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Game game = new Game(
                        rs.getInt("game_id"),
                        rs.getString("game_name"),
                        rs.getString("description"),
                        rs.getInt("genre_id"),
                        rs.getString("genre_name")
                );
                games.add(game);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        gamesTable.setItems(games);
    }

    private void loadReservations() {
        ObservableList<ReservedTime> reservedTimes = FXCollections.observableArrayList();

        String query = "SELECT * FROM Reservations";
        try (Connection conn = databaseConnection.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                // Получаем дату и время как Timestamp для начала и окончания
                java.sql.Timestamp reservedTimeTimestamp = resultSet.getTimestamp("reservation_time");
                java.sql.Timestamp endTimeTimestamp = resultSet.getTimestamp("end_time");

                // Преобразуем Timestamp в LocalDateTime и форматируем в нужный формат
                String reservedTime = reservedTimeTimestamp.toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm"));
                String endTime = endTimeTimestamp.toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm"));

                int userId = resultSet.getInt("user_id");

                int pc_id = resultSet.getInt("computer_id");

                // Добавляем преобразованные данные в список
                reservedTimes.add(new ReservedTime(reservedTime, endTime, String.valueOf(userId), String.valueOf(pc_id)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        reservationsTable.setItems(reservedTimes);
    }


    private void loadOrders() {
        ObservableList<Order> orders = FXCollections.observableArrayList();
        String query = "SELECT * FROM Orders";
        try (Connection conn = databaseConnection.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order(rs.getInt("order_id"), rs.getInt("user_id"), rs.getInt("computer_id"),
                        rs.getTimestamp("start_time"), rs.getTimestamp("end_time"), rs.getDouble("total_cost"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ordersTable.setItems(orders);
    }

    private void setupContextMenuForReservations() {
        // Создаем контекстное меню
        ContextMenu contextMenu = new ContextMenu();

        // Создаем пункты меню
        MenuItem cancelReservation = new MenuItem("Отменить бронь");
        MenuItem confirmReservation = new MenuItem("Подтвердить бронь");

        // Добавляем обработчики событий для пунктов меню
        cancelReservation.setOnAction(event -> handleCancelReservation());
        confirmReservation.setOnAction(event -> handleConfirmReservation());

        // Добавляем пункты меню в контекстное меню
        contextMenu.getItems().addAll(cancelReservation, confirmReservation);

        // Привязываем контекстное меню к строкам таблицы
        reservationsTable.setRowFactory(tv -> {
            TableRow<ReservedTime> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                } else {
                    contextMenu.hide();
                }
            });
            return row;
        });
    }

    private void setupContextMenuForGames() {
        // Создаем контекстное меню
        ContextMenu contextMenu = new ContextMenu();

        // Создаем пункт меню "Удалить"
        MenuItem deleteGame = new MenuItem("Удалить");

        // Устанавливаем обработчик события для пункта меню "Удалить"
        deleteGame.setOnAction(event -> handleDeleteGame());

        // Добавляем пункт меню в контекстное меню
        contextMenu.getItems().add(deleteGame);

        // Привязываем контекстное меню к строкам таблицы
        gamesTable.setRowFactory(tv -> {
            TableRow<Game> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                } else {
                    contextMenu.hide();
                }
            });
            return row;
        });
    }

    private void handleDeleteGame() {
        Game selectedGame = gamesTable.getSelectionModel().getSelectedItem();
        if (selectedGame != null) {
            try (Connection conn = databaseConnection.connect()) {
                // Удаляем выбранную игру из базы данных
                String deleteQuery = "DELETE FROM Games WHERE game_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, selectedGame.getId());
                    stmt.executeUpdate();
                }

                // Обновляем таблицу игр
                loadGames();

                AlertManager.showInfoAlert("Успех", "Игра успешно удалена.");
            } catch (SQLException e) {
                AlertManager.showErrorAlert("Ошибка", "Не удалось удалить игру.");
                e.printStackTrace();
            }
        } else {
            AlertManager.showWarningAlert("Не выбрана игра", "Выберите игру перед удалением.");
        }
    }


    private void handleCancelReservation() {
        ReservedTime selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            try (Connection conn = databaseConnection.connect()) {
                // Конвертация данных
                int userId = Integer.parseInt(selectedReservation.userProperty().get());
                int computerId = Integer.parseInt(selectedReservation.pcIdProperty().get());

                // Извлечение времени начала бронирования из таблицы Reservations
                String fetchReservationQuery = "SELECT reservation_time FROM Reservations WHERE user_id = ? AND computer_id = ?";
                Timestamp reservationTime = null;

                try (PreparedStatement fetchStmt = conn.prepareStatement(fetchReservationQuery)) {
                    fetchStmt.setInt(1, userId);
                    fetchStmt.setInt(2, computerId);

                    try (ResultSet rs = fetchStmt.executeQuery()) {
                        if (rs.next()) {
                            reservationTime = rs.getTimestamp("reservation_time");
                        }
                    }
                }

                // Проверка на случай отсутствия данных
                if (reservationTime == null) {
                    AlertManager.showErrorAlert("Ошибка", "Не удалось найти данные о бронировании в базе данных.");
                    return;
                }

                // Удаление записи из таблицы Reservations
                String deleteReservationQuery = "DELETE FROM Reservations WHERE user_id = ? AND computer_id = ? AND reservation_time = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteReservationQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, computerId);
                    stmt.setTimestamp(3, reservationTime);
                    stmt.executeUpdate();
                }

                // Обновление таблицы Reservations на интерфейсе
                loadReservations();

                AlertManager.showInfoAlert("Успех", "Бронь успешно отменена.");
            } catch (SQLException e) {
                AlertManager.showErrorAlert("Ошибка", "Произошла ошибка при отмене брони.");
                e.printStackTrace();
            }
        } else {
            AlertManager.showWarningAlert("Не выбрана бронь", "Выберите строку перед выполнением действия.");
        }
    }


    private void handleConfirmReservation() {
        ReservedTime selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            try (Connection conn = databaseConnection.connect()) {
                // Конвертация данных
                int userId = Integer.parseInt(selectedReservation.userProperty().get());
                int computerId = Integer.parseInt(selectedReservation.pcIdProperty().get());

                // Извлечение времени начала и конца бронирования из таблицы Reservations
                String fetchReservationQuery = "SELECT reservation_time, end_time FROM Reservations WHERE user_id = ? AND computer_id = ?";
                Timestamp startTime = null;
                Timestamp endTime = null;

                try (PreparedStatement fetchStmt = conn.prepareStatement(fetchReservationQuery)) {
                    fetchStmt.setInt(1, userId);
                    fetchStmt.setInt(2, computerId);

                    try (ResultSet rs = fetchStmt.executeQuery()) {
                        if (rs.next()) {
                            startTime = rs.getTimestamp("reservation_time");
                            endTime = rs.getTimestamp("end_time");
                        }
                    }
                }

                // Проверка на случай отсутствия данных
                if (startTime == null || endTime == null) {
                    AlertManager.showErrorAlert("Ошибка", "Не удалось найти данные о бронировании в базе данных.");
                    return;
                }

                // Вычисление стоимости
                double totalCost = calculateCost(startTime, endTime);

                // SQL-запрос для добавления записи в таблицу Orders
                String insertOrderQuery = "INSERT INTO Orders (user_id, computer_id, start_time, end_time, total_cost) " +
                        "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(insertOrderQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, computerId);
                    stmt.setTimestamp(3, startTime);
                    stmt.setTimestamp(4, endTime);
                    stmt.setDouble(5, totalCost);
                    stmt.executeUpdate();
                }

                // Удаление из Reservations после переноса в Orders
                String deleteReservationQuery = "DELETE FROM Reservations WHERE user_id = ? AND computer_id = ? AND reservation_time = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteReservationQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, computerId);
                    stmt.setTimestamp(3, startTime);
                    stmt.executeUpdate();
                }

                // Обновление таблиц
                loadOrders();
                loadReservations();

                AlertManager.showInfoAlert("Успех", "Бронь подтверждена и добавлена в заказы.");
            } catch (SQLException e) {
                AlertManager.showErrorAlert("Ошибка", "Произошла ошибка при подтверждении брони.");
                e.printStackTrace();
            }
        } else {
            AlertManager.showWarningAlert("Не выбрана бронь", "Выберите строку перед выполнением действия.");
        }
    }


    // Метод для вычисления стоимости
    private double calculateCost(Timestamp startTime, Timestamp endTime) {
        long milliseconds = endTime.getTime() - startTime.getTime();
        double hours = milliseconds / (1000.0 * 60 * 60);
        return Math.ceil(hours) * 100; // Цена за час: 100 рублей
    }

    @FXML
    private void handleAddGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddGameDialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Добавить игру");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Обновление таблицы игр после добавления
            loadGames();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось открыть окно добавления игры.");
            alert.showAndWait();
        }
    }

    public void logout(ActionEvent actionEvent) {
        // Получаем текущий Stage из компонента интерфейса
        Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

        // Вызываем метод Logout для выхода
        Logout.performLogout(currentStage);
    }
}