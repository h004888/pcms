package com.pcms.categoryservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
                @NotBlank(message = "Tên danh mục không được để trống") @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự") String name,
                @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự") String description) {
}
