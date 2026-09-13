package com.example.entity;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "items")
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category = Category.OTHER;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private int minimumQuantity = 1;
    @Column(nullable = false, length = 1000)
    private String note = "";
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void created() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }
    @PreUpdate
    void updated() { updatedAt = Instant.now(); }

    @Transient
    public StockStatus getStatus() {
        if (quantity == 0) return StockStatus.OUT;
        return quantity <= minimumQuantity ? StockStatus.LOW : StockStatus.OK;
    }
    @Transient
    public boolean isNeedsPurchase() { return getStatus() != StockStatus.OK; }
}

