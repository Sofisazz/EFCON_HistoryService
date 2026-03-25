package com.example.historyservice.repository;

import com.example.historyservice.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {

    List<Note> findByUserId(int userId);

    List<Note> findByUserIdAndEatingPlanIdIsNotNull(int userId);

    boolean existsByUserIdAndEatingPlanIdAndRecipeIdAndMarkAndComment(int userId, Integer eatingPlanId, Integer recipeId, Integer mark, String comment);

    boolean existsByUserIdAndRecipeIdAndMarkAndComment(int userId, Integer recipeId, Integer mark, String comment);

    boolean existsByIdAndUserId(int id, int userId);
}
