package com.example.entity;

public enum Category {
    TOILETRIES("洗面用品"), BATH("お風呂用品"), LAUNDRY("洗濯用品"),
    CLEANING("掃除用品"), KITCHEN("キッチン用品"), PAPER("ティッシュ・紙類"), OTHER("その他");

    private final String label;
    Category(String label) { this.label = label; }
    public String getLabel() { return label; }
}
