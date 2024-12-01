package tarasov.app.pc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private TableColumn<ReservedTime, String> reservationIdColumn;
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

//        reservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        computerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        reservationTimeColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//
//        orderIdColumn.setCellValueFactory(cellData -> cellData.getValue().orderIdProperty().asObject());
//        orderUserIdColumn.setCellValueFactory(cellData -> cellData.getValue().orderUserIdProperty().asObject());
//        orderComputerIdColumn.setCellValueFactory(cellData -> cellData.getValue().orderComputerIdProperty().asObject());
//        orderStartTimeColumn.setCellValueFactory(cellData -> cellData.getValue().orderStartTimeProperty());
//        orderEndTimeColumn.setCellValueFactory(cellData -> cellData.getValue().orderEndTimeProperty());
//        totalCostColumn.setCellValueFactory(cellData -> cellData.getValue().totalCostProperty().asObject());

    }

    // Метод для инициализации данных в таблицах
    public void initialize() {
    }

    private void loadGames() {
        ObservableList<Game> games = FXCollections.observableArrayList();
        String query = "SELECT games.game_id, games.game_name, games.description, genres.id AS genre_id, genres.name AS genre_name " +
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

                // Добавляем преобразованные данные в список
                reservedTimes.add(new ReservedTime(reservedTime, endTime, String.valueOf(userId)));
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
}