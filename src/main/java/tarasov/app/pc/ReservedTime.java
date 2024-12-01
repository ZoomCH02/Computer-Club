package tarasov.app.pc;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReservedTime {
    private final StringProperty time;
    private final StringProperty endTime;
    private final StringProperty user;

    public ReservedTime(String time, String endTime, String user) {
        this.time = new SimpleStringProperty(time);
        this.endTime = new SimpleStringProperty(endTime);
        this.user = new SimpleStringProperty(user);
    }

    public StringProperty timeProperty() {
        return time;
    }

    public StringProperty endTimeProperty() {
        return endTime;
    }

    public StringProperty userProperty() {
        return user;
    }
}