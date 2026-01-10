package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.Item;
import com.example.repository.ItemRepository;

@Service
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public Item create(String name, int quantity) {
        Item item = new Item(null, name, quantity);
        return repository.save(item);
    }

    public List<Item> findAll() {
        return repository.findAll();
    }

    public Item updateQuantity(Long id, int quantity) {
        Item item = repository.findById(id);

        if (item == null) {
            throw new RuntimeException("Item not found: id=" + id);
        }

        item.setQuantity(quantity);
        return item;
    }
 // ★ + / - 用
    public void changeQuantity(Long id, int delta) {
        Item item = repository.findById(id);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }

        int newQuantity = item.getQuantity() + delta;
        if (newQuantity < 0) {
            newQuantity = 0; // 最低0
        }

        repository.updateQuantity(id, newQuantity);
    }

    // ★ 削除
    public void delete(Long id) {
        repository.delete(id);
    }
}
