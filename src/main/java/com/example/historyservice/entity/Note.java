package com.example.historyservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private int id;

    @NotNull(message = "Id пользователя обязателен")
    @Min(value = 1, message = "Id пользователя не может быть меньше 1")
    @Column(nullable = false)
    private Integer userId;

    @NotNull(message = "Оценка обязательна (1-5 баллов)")
    @Max(value = 5, message = "Оценка должна быть не выше 5")
    @Min(value = 1, message = "Оценка должна быть не ниже 1")
    @Column(nullable = false)
    private Integer mark;

    @Size(min = 10, max = 1000, message = "Количество символов от 10 до 1000")
    private String comment;
}
