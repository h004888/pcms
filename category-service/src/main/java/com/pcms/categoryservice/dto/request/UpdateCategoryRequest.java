package com.pcms.categoryservice.dto.request;

import com.pcms.categoryservice.enums.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
                @NotBlank(message = "Tên danh mục không được để trống") @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự") String name,
                @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự") String description,
                CategoryStatus status) {
}
