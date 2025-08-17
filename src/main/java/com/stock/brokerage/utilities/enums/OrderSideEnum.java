package com.stock.brokerage.utilities.enums;

public enum OrderSideEnum {
    BUY(1, "BUY"),
    SELL(2, "SELL");
    private int id;
    private String description;
    OrderSideEnum(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public static OrderStatusEnum fromId(int id) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.getId() == id) return status;
        }
        throw new IllegalArgumentException("Invalid id: " + id);
    }
}
