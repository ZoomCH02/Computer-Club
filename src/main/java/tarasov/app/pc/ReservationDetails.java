package tarasov.app.pc;

import java.sql.Timestamp;

public class ReservationDetails {
    private final Timestamp startTime;
    private final Timestamp endTime;

    public ReservationDetails(Timestamp startTime, Timestamp endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }
}
