package com.sketchproject.infogue.models;

/**
 * Sketch Project Studio
 * Created by angga on 21/09/16.
 */
public class Bank {
    public static final String TABLE = "banks";
    public static final String ID = "id";
    public static final String BANK = "bank";
    public static final String LOGO = "logo";

    private int id;
    private String bank;
    private String logo;

    public Bank() {
    }

    public Bank(int id, String bank, String logo) {
        this.id = id;
        this.bank = bank;
        this.logo = logo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
