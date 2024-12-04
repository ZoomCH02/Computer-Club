package tarasov.app.pc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ReserveModalController {

    /*============ПОЛЯ И ГРАФИЧЕСКИЕ КОМПОНЕНТЫ===========*/
    @FXML
    private ComboBox<String> startTimeComboBox; // ComboBox для выбора времени начала
    @FXML
    private ComboBox<String> endTimeComboBox; // ComboBox для выбора времени окончания
    @FXML
    private DatePicker reservationDatePicker; // Поле для выбора даты
    @FXML
    private TableView<ReservedTime> reservedTimesTable; // Таблица с занятыми временами
    @FXML
    private TableColumn<ReservedTime, String> timeColumn; // Колонка для времени
    @FXML
    private TableColumn<ReservedTime, String> userColumn; // Колонка для пользователя
    @FXML
    private TableColumn<ReservedTime, String> endTimeColumn; // Колонка для времени окончания
    @FXML
    private TableColumn<ReservedTime, String> pcColumn; // Колонка для компьютера

    private Computer selectedComputer;
    private int userId;
    private final DatabaseConnection databaseConnection = new DatabaseConnection();

    /*============НАСТРОЙКА КОМПЬЮТЕРА И ИНИЦИАЛИЗАЦИЯ===========*/
    public void setSelectedComputer(Computer computer, int userId) {
        this.selectedComputer = computer;
        this.userId = userId;

        // Устанавливаем ограничения на выбор даты
        reservationDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Слушатель изменений даты
        reservationDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.equals(LocalDate.now())) {
                    fillTimeComboBoxes(LocalTime.now().getHour(), 23); // Сегодняшняя дата
                } else {
                    fillTimeComboBoxes(0, 23); // Любая другая дата
                }
                loadReservedTimes();
            }
        });

        // Настройка колонок таблицы
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        endTimeColumn.setCellValueFactory(cellData -> cellData.getValue().endTimeProperty());
        userColumn.setCellValueFactory(cellData -> cellData.getValue().userProperty());
        pcColumn.setCellValueFactory(cellData -> cellData.getValue().pcIdProperty());

        loadReservedTimes(); // Загрузка данных в таблицу
    }

    /*============ИНИЦИАЛИЗАЦИЯ КОМПОНЕНТОВ===========*/
    @FXML
    public void initialize() {
        // Метод оставлен пустым для возможности добавления действий при инициализации
    }

    /*============НАПОЛНЕНИЕ ВРЕМЕННЫХ COMBOBOX===========*/
    private void fillTimeComboBoxes(int startHour, int endHour) {
        ObservableList<String> times = FXCollections.observableArrayList();

        for (int i = startHour; i <= endHour; i++) {
            String time = String.format("%02d:00", i);
            times.add(time);
        }

        startTimeComboBox.setItems(times);
        startTimeComboBox.setValue(times.get(0));
        updateEndTimeComboBox(times.get(0));

        startTimeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateEndTimeComboBox(newValue);
        });
    }

    private void updateEndTimeComboBox(String startTimeText) {
        ObservableList<String> timesForEnd = FXCollections.observableArrayList();
        LocalTime startTime = LocalTime.parse(startTimeText, DateTimeFormatter.ofPattern("HH:mm"));

        for (int i = startTime.getHour() + 1; i < 24; i++) {
            timesForEnd.add(String.format("%02d:00", i));
        }

        endTimeComboBox.setItems(timesForEnd);
        if (!timesForEnd.isEmpty()) {
            endTimeComboBox.setValue(timesForEnd.get(0));
        }
    }

    /*============ЗАГРУЗКА ЗАНЯТЫХ ВРЕМЕН ИЗ БД===========*/
    private void loadReservedTimes() {
        ObservableList<ReservedTime> reservedTimes = FXCollections.observableArrayList();

        // Запрос для загрузки данных из Reservations
        String reservationsQuery =
                "SELECT reservation_time, end_time, user_id, computer_id FROM Reservations " +
                        "WHERE computer_id = ? AND reservation_time LIKE ?";

        // Запрос для загрузки данных из Orders
        String ordersQuery =
                "SELECT start_time, end_time, user_id, computer_id FROM Orders " +
                        "WHERE computer_id = ? AND start_time LIKE ?";

        LocalDate selectedDate = reservationDatePicker.getValue();

        if (selectedDate != null) {
            try (Connection conn = databaseConnection.connect()) {
                // Загрузка данных из таблицы Reservations
                loadDataFromTable(conn, reservationsQuery, selectedDate, reservedTimes, "reserv");

                // Загрузка данных из таблицы Orders
                loadDataFromTable(conn, ordersQuery, selectedDate, reservedTimes, "order");

                // Установка данных в таблицу
                reservedTimesTable.setItems(reservedTimes);

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке занятых времен: " + e.getMessage());
            }
        }
    }

    /*============ПОМОЩНИК ДЛЯ ЗАГРУЗКИ ДАННЫХ ИЗ ТАБЛИЦЫ===========*/
    private void loadDataFromTable(Connection conn, String query, LocalDate selectedDate, ObservableList<ReservedTime> reservedTimes, String table_name) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, selectedComputer.getId());
            stmt.setString(2, selectedDate + "%");

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                String reservedTime;
                if ("reserv".equals(table_name)) {
                    reservedTime = resultSet.getTimestamp("reservation_time").toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm"));
                } else if ("order".equals(table_name)) {
                    reservedTime = resultSet.getTimestamp("start_time").toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm"));
                } else {
                    throw new IllegalArgumentException("Unsupported table name: " + table_name);
                }

                String endTime = resultSet.getTimestamp("end_time").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm"));
                int userId = resultSet.getInt("user_id");
                int pcId = resultSet.getInt("computer_id");

                reservedTimes.add(new ReservedTime(reservedTime, endTime, String.valueOf(userId), String.valueOf(pcId)));
            }

        }
    }


    /*============ОБРАБОТЧИК КНОПКИ "ЗАРЕЗЕРВИРОВАТЬ"===========*/
    public void handleReserve() {
        LocalDate selectedDate = reservationDatePicker.getValue();
        String startTimeText = startTimeComboBox.getValue();
        String endTimeText = endTimeComboBox.getValue();

        if (selectedDate == null || startTimeText == null || endTimeText == null) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Пожалуйста, выберите дату и время.");
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime startTime = LocalTime.parse(startTimeText, formatter);
            LocalTime endTime = LocalTime.parse(endTimeText, formatter);

            if (startTime.isAfter(endTime)) {
                showAlert(Alert.AlertType.WARNING, "Ошибка", "Время окончания должно быть позже времени начала.");
                return;
            }

            String reservationTime = selectedDate.atTime(startTime).toString();
            String endTimeStr = selectedDate.atTime(endTime).toString();

            String query = "INSERT INTO Reservations (computer_id, user_id, reservation_time, end_time) VALUES (?, ?, ?, ?)";
            try (Connection conn = databaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, selectedComputer.getId());
                stmt.setInt(2, userId);
                stmt.setString(3, reservationTime);
                stmt.setString(4, endTimeStr);

                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Резервация выполнена.");
                loadReservedTimes();
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при создании резервации: " + e.getMessage());
        }
    }


    /*============ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ПОКАЗА АЛЕРТОВ===========*/
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
