package com.example.historyservice.dto;

import lombok.Data;

@Data
public class NoteFullRecipeDto {
    private int id;
    private Integer userId;
    private String recipeName;
    private Double calories;
    private Integer mark;
    private String comment;
}