package com.example.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.example.entity.Item;

@Repository
public class ItemRepository {

    private final Map<Long, Item> store = new HashMap<>();
    private long sequence = 1L;

    public Item save(Item item) {
        item.setId(sequence++);
        store.put(item.getId(), item);
        return item;
    }

    public List<Item> findAll() {
        return new ArrayList<>(store.values());
    }

    public Item findById(Long id) {
        return store.get(id);
    }
    // ★ 数量更新
    public void updateQuantity(Long id, int quantity) {
        Item item = store.get(id);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }
        item.setQuantity(quantity);
    }

    // ★ 削除
    public void delete(Long id) {
        store.remove(id);
    }
}

