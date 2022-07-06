package com.desti.currencyconverter;

public class CurrencyModel {
    private String currency;
    private boolean isChecked;
    private String description;

    public CurrencyModel(String currency, String description, boolean isChecked) {
        this.currency = currency;
        this.description = description;
        this.isChecked = isChecked;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
