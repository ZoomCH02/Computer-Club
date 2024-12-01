package tarasov.app.pc;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReservedTime {
    private final StringProperty time;
    private final StringProperty endTime;
    private final StringProperty user;
    private final StringProperty pc_id;

    public ReservedTime(String time, String endTime, String user, String pc_id) {
        this.time = new SimpleStringProperty(time);
        this.endTime = new SimpleStringProperty(endTime);
        this.user = new SimpleStringProperty(user);
        this.pc_id = new SimpleStringProperty(pc_id);
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

    public StringProperty pcIdProperty() {
        return pc_id;
    }
}
