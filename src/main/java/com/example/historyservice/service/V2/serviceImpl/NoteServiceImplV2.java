package com.example.historyservice.service.V2.serviceImpl;

import com.example.historyservice.dto.*;
import com.example.historyservice.dto.mapping.NoteMapper;
import com.example.historyservice.entity.Note;
import com.example.historyservice.exceptions.ExistException;
import com.example.historyservice.exceptions.MissingException;
import com.example.historyservice.feignclient.MenuClient;
import com.example.historyservice.feignclient.UserClient;
import com.example.historyservice.repository.NoteRepository;
import com.example.historyservice.service.V2.NoteServiceV2;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImplV2 implements NoteServiceV2 {


    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    private final MenuClient menuClient;
    private final UserClient userClient;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public List<NoteFullRecipeDto> findFullHistoryByUserIdForRecipe(int userId) {
        circuitBreakerUserExists(userId);

        List<Note> notes = noteRepository.findByUserId(userId);

        List<NoteFullRecipeDto> noteFullList = new ArrayList<>();

        for (Note note : notes) {
            NoteFullRecipeDto noteFullRecipeDto = createFullNoteRecipeDto(note);
            noteFullList.add(noteFullRecipeDto);
        }

        return noteFullList;
    }

    @Override
    public List<NoteFullPlanDto> findFullHistoryByUserIdForPlans(int userId) {
        circuitBreakerUserExists(userId);

        List<Note> notes = noteRepository.findByUserIdAndEatingPlanIdIsNotNull(userId);

        List<NoteFullPlanDto> noteFullList = new ArrayList<>();

        for (Note note : notes) {
            NoteFullPlanDto noteFullPlanDto = createFullNotePlanDto(note);
            noteFullList.add(noteFullPlanDto);
        }

        return noteFullList;
    }

    @Transactional
    @Override
    public NoteDto createNoteForUser(NoteDto noteDto, int userId) {
        circuitBreakerUserExists(userId);

        if (noteDto.getEatingPlanId() != null) {
            checkEatingPlanForCreateNote(noteDto, userId);
        } else {
            checkRecipeForCreateNote(noteDto, userId);
        }

        Note note = noteMapper.toEntity(noteDto);
        note.setUserId(userId);
        noteRepository.save(note);

        return noteMapper.toDto(note);
    }

    @Transactional
    @Override
    public void deleteNote(int id, int userId) {
        circuitBreakerUserExists(userId);

        if (!noteRepository.existsByIdAndUserId(id, userId)) {
            throw new MissingException("Заметки с id '" + id + "' для пользователя с id '" + userId + "' не существует");
        }

        noteRepository.deleteById(id);
    }

    private void checkRecipeForCreateNote(NoteDto noteDto, int userId) {
        circuitBreakerGetRecipe(noteDto.getRecipeId(), userId);

        boolean isDuplicate = noteRepository.existsByUserIdAndRecipeIdAndMarkAndComment(userId, noteDto.getRecipeId(), noteDto.getMark(), noteDto.getComment());

        if (isDuplicate) {
            throw new ExistException("Вы уже оставляли такую оценку этому рецепту вне плана.");
        }

    }

    private void checkEatingPlanForCreateNote(NoteDto noteDto, int userId) {
        circuitBreakerEatingPlanExistsWithRecipe(noteDto.getEatingPlanId(), noteDto.getRecipeId(), userId);

        boolean isDuplicate = noteRepository.existsByUserIdAndEatingPlanIdAndRecipeIdAndMarkAndComment(userId, noteDto.getEatingPlanId(), noteDto.getRecipeId(), noteDto.getMark(), noteDto.getComment());

        if (isDuplicate) {
            throw new ExistException("Вы уже оставляли такую оценку этому рецепту в рамках этого плана.");
        }
    }

    private NoteFullRecipeDto createFullNoteRecipeDto(Note note){
        RecipeDto recipe = circuitBreakerGetRecipe(note.getRecipeId(), note.getUserId());

        Optional.ofNullable(recipe)
                .orElseThrow(() -> new MissingException("Menu сервис временно недоступен"));

        NoteFullRecipeDto fullDto = new NoteFullRecipeDto();

        fullDto.setId(note.getId());
        fullDto.setUserId(note.getUserId());
        fullDto.setMark(note.getMark());
        fullDto.setComment(note.getComment());
        fullDto.setRecipeName(recipe.getName());
        fullDto.setCalories(recipe.getCaloriesFor100());

        return fullDto;
    }


    private NoteFullPlanDto createFullNotePlanDto(Note note){
        NoteFullPlanDto fullDto = new NoteFullPlanDto();

        fullDto.setId(note.getId());
        fullDto.setUserId(note.getUserId());
        fullDto.setMark(note.getMark());
        fullDto.setComment(note.getComment());


        if (note.getEatingPlanId() != null) {
            EatingPlanDto plan = circuitBreakerGetEatingPlan(note.getEatingPlanId(), note.getUserId());

            fullDto.setType(plan.getType());
            fullDto.setNumberOfPeople(plan.getNumberOfPeople());

            if ("OUTSIDE".equals(plan.getStatus())) {
                fullDto.setLocation("OUTSIDE");
            } else {
                fullDto.setLocation("HOME");
            }
        }

        if (note.getRecipeId() != null) {
                RecipeDto recipe = circuitBreakerGetRecipe(note.getRecipeId(), note.getUserId());

                fullDto.setRecipeName(recipe.getName());
        }

        return fullDto;
    }

    private void circuitBreakerUserExists(Integer userId){
        boolean exists = executeWithCircuitBreaker("userService", () -> userClient.checkUserExists(userId));

        if (!exists) {
            throw new MissingException("Пользователя с id '" + userId + "' не существует");
        }
    }

    private EatingPlanDto circuitBreakerGetEatingPlan(int planId, int userId) {
        EatingPlanDto eatingPlan = executeWithCircuitBreaker("menuService", () -> menuClient.getEatingPlan(planId, userId));

        Optional.ofNullable(eatingPlan)
                .orElseThrow(() -> new MissingException("План питания с id '" + planId + "' для пользователя с id '" + userId + "' не существует"));

        return eatingPlan;
    }

    private void circuitBreakerEatingPlanExistsWithRecipe(int planId, int recipeId, int userId) {
        boolean exists = executeWithCircuitBreaker("menuService", () -> menuClient.existEatingPlanWithRecipe(planId, recipeId, userId));

        if (!exists) {
            throw new MissingException("Рецепт с id '" + recipeId + "' не входит в состав плана питания с id '" + planId + "'");
        }
    }

    private RecipeDto circuitBreakerGetRecipe(int recipeId, int userId) {
        RecipeDto recipe = executeWithCircuitBreaker("menuService", () -> menuClient.getRecipeById(recipeId, userId));

        Optional.ofNullable(recipe)
                .orElseThrow(() -> new MissingException("Рецепт с id '" + recipeId + "' для пользователя с id '" + userId + "' не существует"));

        return recipe;
    }

    private <T> T executeWithCircuitBreaker(String circuitBreakerName, Supplier<T> supplier) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(cb, supplier);

        try {
            return decoratedSupplier.get();
        } catch (CallNotPermittedException e) {

            log.warn("Circuit Breaker '{}' разомкнут. Сервис недоступен", circuitBreakerName);
            throw new MissingException(circuitBreakerName + " временно недоступен");
        } catch (MissingException e) {

            throw e;
        } catch (Exception e) {

            log.error("Ошибка при вызове сервиса через CB '{}': {}", circuitBreakerName, e.getMessage(), e);
            throw new MissingException("Ошибка связи с " + circuitBreakerName);
        }
    }
}
