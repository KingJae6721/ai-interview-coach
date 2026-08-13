package com.aiinterview.dashboard.dto;

public enum DashboardAnalyticsPeriod {
    WEEKLY("week"),
    MONTHLY("month");

    private final String dateTruncUnit;

    DashboardAnalyticsPeriod(String dateTruncUnit) {
        this.dateTruncUnit = dateTruncUnit;
    }

    public String getDateTruncUnit() {
        return dateTruncUnit;
    }
}
