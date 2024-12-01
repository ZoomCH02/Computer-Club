package tarasov.app.pc;

import java.sql.Timestamp;

public class Order {
    private int orderId;
    private int userId;
    private int computerId;
    private Timestamp startTime;
    private Timestamp endTime;
    private double totalCost;

    public Order(int orderId, int userId, int computerId, Timestamp startTime, Timestamp endTime, double totalCost) {
        this.orderId = orderId;
        this.userId = userId;
        this.computerId = computerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalCost = totalCost;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public int getComputerId() {
        return computerId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public double getTotalCost() {
        return totalCost;
    }
}