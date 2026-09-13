package com.example.dto;

import com.example.entity.Category;
import com.example.entity.Item;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ItemForm {
    @NotBlank(message = "商品名を入力してください。")
    @Size(max = 100, message = "商品名は100文字以内で入力してください。")
    private String name;
    @NotNull(message = "カテゴリを選択してください。")
    private Category category = Category.OTHER;
    @NotNull(message = "現在のストック数を入力してください。")
    @Min(value = 0, message = "0以上の整数を入力してください。")
    @Max(value = 9999, message = "9999以下の整数を入力してください。")
    private Integer quantity = 0;
    @NotNull(message = "最低ストック数を入力してください。")
    @Min(value = 0, message = "0以上の整数を入力してください。")
    @Max(value = 9999, message = "9999以下の整数を入力してください。")
    private Integer minimumQuantity = 1;
    @Size(max = 1000, message = "メモは1000文字以内で入力してください。")
    private String note = "";

    public static ItemForm from(Item item) {
        ItemForm form = new ItemForm();
        form.setName(item.getName());
        form.setCategory(item.getCategory());
        form.setQuantity(item.getQuantity());
        form.setMinimumQuantity(item.getMinimumQuantity());
        form.setNote(item.getNote());
        return form;
    }
}
