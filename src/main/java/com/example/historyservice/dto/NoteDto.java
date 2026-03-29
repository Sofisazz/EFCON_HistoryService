package com.example.historyservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoteDto {
    private int id;

    @NotNull(message = "ID рецепта обязателен")
    private Integer recipeId;

    private Integer eatingPlanId;

    @NotNull(message = "Оценка обязательна")
    @Min(value = 1, message = "Оценка должна быть от 1 до 5")
    @Max(value = 5, message = "Оценка должна быть от 1 до 5")
    private Integer mark;

    @Size(min = 10, max = 1000, message = "Комментарий должен быть от 10 до 1000 символов")
    private String comment;
}
