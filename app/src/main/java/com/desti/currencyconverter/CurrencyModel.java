package com.desti.currencyconverter;

public class CurrencyModel {
    private String currency;
    private boolean isChecked;

    public CurrencyModel(String currency, boolean isChecked) {
        this.currency = currency;
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
}
