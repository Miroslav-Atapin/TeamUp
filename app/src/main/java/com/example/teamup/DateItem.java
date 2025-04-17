package com.example.teamup;

public class DateItem {
    private String dayOfWeek;
    private String dateNumber;
    private boolean isSelected;

    public DateItem(String dayOfWeek, String dateNumber) {
        this.dayOfWeek = dayOfWeek;
        this.dateNumber = dateNumber;
        this.isSelected = false;
    }

    // Геттеры и сеттеры
    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDateNumber() {
        return dateNumber;
    }

    public void setDateNumber(String dateNumber) {
        this.dateNumber = dateNumber;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}