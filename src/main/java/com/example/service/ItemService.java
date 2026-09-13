package com.example.service;

import java.util.Comparator;
import java.util.List;
import com.example.dto.ItemForm;
import com.example.entity.Category;
import com.example.entity.Item;
import com.example.repository.ItemRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository repository;
    public ItemService(ItemRepository repository) { this.repository = repository; }

    public List<Item> findAll() { return repository.findAllByOrderByCreatedAtAscIdAsc(); }
    public List<Item> list(Category category, boolean shopping) {
        return findAll().stream()
            .filter(item -> category == null || item.getCategory() == category)
            .filter(item -> !shopping || item.isNeedsPurchase())
            .sorted(Comparator.comparing(Item::getStatus).thenComparing(Item::getId))
            .toList();
    }
    public Item find(Long id) {
        return repository.findById(id).orElseThrow(ItemService::notFound);
    }
    @Transactional
    public Item create(@NotNull @Valid ItemForm form) {
        Item item = new Item();
        apply(item, form);
        return repository.save(item);
    }
    @Transactional
    public void edit(Long id, @NotNull @Valid ItemForm form) {
        apply(locked(id), form);
    }
    @Transactional
    public Item updateQuantity(Long id, @NotNull @Min(0) @Max(9999) Integer quantity) {
        Item item = locked(id);
        item.setQuantity(quantity);
        return item;
    }
    @Transactional
    public void changeQuantity(Long id, int delta) {
        if (delta != 1 && delta != -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量は1ずつ変更してください。");
        }
        Item item = locked(id);
        item.setQuantity(Math.clamp(item.getQuantity() + delta, 0, 9999));
    }
    @Transactional
    public void delete(Long id) { repository.delete(locked(id)); }

    public String shoppingText(List<Item> items) {
        StringBuilder text = new StringBuilder("購入リスト\n");
        for (Item item : items) {
            text.append("・").append(item.getName()).append("（現在のストック：")
                .append(item.getQuantity()).append("個）\n");
        }
        if (items.isEmpty()) text.append("購入が必要な商品はありません。\n");
        return text.toString();
    }
    private Item locked(Long id) {
        return repository.findForUpdate(id).orElseThrow(ItemService::notFound);
    }
    private void apply(Item item, ItemForm form) {
        item.setName(form.getName().strip());
        item.setCategory(form.getCategory());
        item.setQuantity(form.getQuantity());
        item.setMinimumQuantity(form.getMinimumQuantity());
        item.setNote(form.getNote() == null ? "" : form.getNote().strip());
    }
    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "商品が見つかりません。");
    }
}
