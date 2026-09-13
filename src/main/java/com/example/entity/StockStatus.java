package com.example.entity;

public enum StockStatus {
    OUT("在庫切れ", "out"), LOW("残りわずか", "low"), OK("在庫あり", "ok");

    private final String label;
    private final String cssClass;
    StockStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }
    public String getLabel() { return label; }
    public String getCssClass() { return cssClass; }
}
