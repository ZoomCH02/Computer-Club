package tarasov.app.pc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ReserveModalController {

    @FXML
    private ComboBox<String> startTimeComboBox; // ComboBox для выбора времени начала
    @FXML
    private ComboBox<String> endTimeComboBox; // ComboBox для выбора времени окончания
    @FXML
    private DatePicker reservationDatePicker; // Поле для выбора даты
    @FXML
    private TableView<ReservedTime> reservedTimesTable; // Таблица с занятыми временем
    @FXML
    private TableColumn<ReservedTime, String> timeColumn; // Колонка для времени
    @FXML
    private TableColumn<ReservedTime, String> userColumn; // Колонка для пользователя
    @FXML
    private TableColumn<ReservedTime, String> endTimeColumn; // Колонка для времени окончания

    private Computer selectedComputer;
    private int userId;

    private final DatabaseConnection databaseConnection = new DatabaseConnection();

    public void setSelectedComputer(Computer computer, int userId) {
        this.selectedComputer = computer;
        this.userId = userId;

        ObservableList<String> times = FXCollections.observableArrayList();

        LocalTime currentTime = LocalTime.now(); // Текущее время
        int currentHour = currentTime.getHour(); // Текущий час

        // Добавляем время, начиная с текущего часа
        for (int i = currentHour; i < 24; i++) {
            String time = String.format("%02d:00", i); // Форматируем время как 00:00, 01:00 и т.д.
            times.add(time);
        }

        startTimeComboBox.setItems(times);
        startTimeComboBox.setValue(times.get(0)); // Первое доступное время

        // Устанавливаем значения для endTimeComboBox в зависимости от startTimeComboBox
        updateEndTimeComboBox(times.get(0));

        startTimeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateEndTimeComboBox(newValue); // Обновление времени окончания при изменении времени начала
        });

        // Устанавливаем минимальную дату для DatePicker (невозможно выбрать вчерашний день)
        reservationDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Устанавливаем минимальную дату (не разрешаем выбирать вчерашний день)
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Инициализация таблицы с занятыми временем
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        endTimeColumn.setCellValueFactory(cellData -> cellData.getValue().endTimeProperty()); // Привязка для времени окончания
        userColumn.setCellValueFactory(cellData -> cellData.getValue().userProperty());

        // Загружаем занятые времена
        loadReservedTimes();

        // Слушатель изменения даты в DatePicker
        reservationDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadReservedTimes(); // Загружаем занятые времена для новой даты
            }
        });
    }

    // Инициализация ComboBox с предустановленными значениями времени и таблицы
    @FXML
    public void initialize() {

    }


    // Обновление значений для endTimeComboBox в зависимости от времени начала
    private void updateEndTimeComboBox(String startTimeText) {
        ObservableList<String> timesForEnd = FXCollections.observableArrayList();

        // Получаем выбранное время начала
        LocalTime startTime = LocalTime.parse(startTimeText, DateTimeFormatter.ofPattern("HH:mm"));
        // Добавляем возможные значения для окончания, начиная с 1 часа позже выбранного времени начала
        for (int i = startTime.getHour() + 1; i < 24; i++) {
            String time = String.format("%02d:00", i);
            timesForEnd.add(time);
        }

        endTimeComboBox.setItems(timesForEnd);
        if (!timesForEnd.isEmpty()) {
            endTimeComboBox.setValue(timesForEnd.get(0)); // Значение по умолчанию - первое доступное время
        }
    }

    // Загрузка занятых времен для выбранного компьютера и даты
    private void loadReservedTimes() {
        ObservableList<ReservedTime> reservedTimes = FXCollections.observableArrayList();

        String query = "SELECT reservation_time, end_time, user_id FROM Reservations WHERE computer_id = ? AND reservation_time LIKE ?";

        LocalDate selectedDate = reservationDatePicker.getValue();

        if (selectedDate != null) {
            try (Connection conn = databaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, selectedComputer.getId());

                // Передаем дату в параметр запроса
                stmt.setString(2, selectedDate + "%");

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

                reservedTimesTable.setItems(reservedTimes);

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке занятых времен: " + e.getMessage());
            }
        }
    }


    // Обработчик для кнопки "Зарезервировать"
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

            // Преобразование в формат DATETIME для базы данных
            String reservationTime = selectedDate.atTime(startTime).toString();
            String endTimeStr = selectedDate.atTime(endTime).toString();

            // Проверка, существует ли уже резервация на это время
            String checkQuery = "SELECT COUNT(*) FROM Reservations WHERE computer_id = ? AND reservation_time < ? AND end_time > ?";
            try (Connection conn = databaseConnection.connect();
                 PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

                checkStmt.setInt(1, selectedComputer.getId());
                checkStmt.setString(2, endTimeStr);  // Проверка, что время окончания нового резервации позже, чем время начала существующей
                checkStmt.setString(3, reservationTime);  // Проверка, что время начала нового резервации раньше, чем время окончания существующей

                ResultSet resultSet = checkStmt.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    showAlert(Alert.AlertType.WARNING, "Ошибка", "В это время уже существует резервация.");
                    return;
                }
            }

            // Вставка данных о резервации в базу данных
            String query = "INSERT INTO Reservations (computer_id, user_id, reservation_time, end_time) VALUES (?, ?, ?, ?)";
            try (Connection conn = databaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, selectedComputer.getId());
                stmt.setInt(2, userId);
                stmt.setString(3, reservationTime);
                stmt.setString(4, endTimeStr);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Резервация выполнена.");
                    loadReservedTimes(); // Обновить таблицу занятых временных интервалов
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при создании резервации: " + e.getMessage());
        }
    }


    // Показать алерт
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
