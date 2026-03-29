package com.example.historyservice.dto;


import lombok.Data;

@Data
public class RecipeIngredientDto {
    private String name;
    private double quantity;
    private String unit;
}
