module com.example.laba_db3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens tarasov.app.pc to javafx.fxml;
    exports tarasov.app.pc;
}