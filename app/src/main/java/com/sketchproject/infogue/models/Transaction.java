package com.sketchproject.infogue.models;

import java.math.BigDecimal;

/**
 * Sketch Project Studio
 * Created by angga on 20/09/16.
 */
public class Transaction {
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String DESCRIPTION = "description";
    public static final String STATUS = "status";
    public static final String AMOUNT = "amount";
    public static final String DATE = "created_at";

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PROCEED = "proceed";
    public static final String STATUS_CANCEL = "cancel";
    public static final String STATUS_SUCCESS = "success";

    public static final String TYPE_REWARD = "reward";
    public static final String TYPE_WITHDRAWAL = "withdrawal";

    private int id;
    private String type;
    private String description;
    private String status;
    private BigDecimal amount;
    private String date;

    public Transaction() {
    }

    public Transaction(int id, String type, String description, String status, BigDecimal amount) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.status = status;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
