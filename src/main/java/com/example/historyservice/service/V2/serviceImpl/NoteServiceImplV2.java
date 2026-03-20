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
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImplV2 implements NoteServiceV2 {


    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    private final MenuClient menuClient;
    private final UserClient userClient;

    @Override
    public List<NoteFullRecipeDto> findFullHistoryByUserIdForRecipe(int userId) {
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
        if (!userClient.checkUserExists(userId)) {
            throw new MissingException("Пользователь с id '" + userId + "' не найден");
        }

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
        if (!userClient.checkUserExists(userId)) {
            throw new MissingException("Пользователь с id '" + userId + "' не найден");
        }

        if (!noteRepository.existByIdAndUserId(id, userId)) {
            throw new MissingException("Заметки с id '" + id + "' для пользователя с id '" + userId + "' не существует");
        }

        noteRepository.deleteById(id);
    }

    private void checkRecipeForCreateNote(NoteDto noteDto, int userId) {
        try {
            menuClient.getRecipeById(noteDto.getRecipeId(), userId);
        } catch (FeignException e) {
            throw new MissingException("Рецепт не найден или недоступен.");
        }

        boolean isDuplicate = noteRepository.existsByUserIdAndRecipeIdAndMarkAndComment(userId, noteDto.getRecipeId(), noteDto.getMark(), noteDto.getComment());

        if (isDuplicate) {
            throw new ExistException("Вы уже оставляли такую оценку этому рецепту вне плана.");
        }

    }

    private void checkEatingPlanForCreateNote(NoteDto noteDto, int userId) {
        try {
            boolean existsInPlan = menuClient.existEatingPlanWithRecipe(noteDto.getEatingPlanId(), noteDto.getRecipeId(), userId);

            if (!existsInPlan) {
                throw new MissingException("Рецепт с id '" + noteDto.getRecipeId() + "' не входит в состав плана питания с id '" + noteDto.getEatingPlanId() + "'");
            }
        } catch (FeignException e) {
            throw new MissingException("Ошибка при проверке состава плана питания.");
        }

        boolean isDuplicate = noteRepository.existsByUserIdAndEatingPlanIdAndRecipeIdAndMarkAndComment(userId, noteDto.getEatingPlanId(), noteDto.getRecipeId(), noteDto.getMark(), noteDto.getComment());

        if (isDuplicate) {
            throw new ExistException("Вы уже оставляли такую оценку этому рецепту в рамках этого плана.");
        }
    }

    private NoteFullRecipeDto createFullNoteRecipeDto(Note note){
        RecipeDto recipe = menuClient.getRecipeById(note.getRecipeId(), note.getUserId());
        NoteFullRecipeDto fullDto = new NoteFullRecipeDto();

        fullDto.setId(note.getId());
        fullDto.setUserId(note.getUserId());
        fullDto.setRecipeName(recipe.getName());
        fullDto.setCalories(recipe.getCaloriesFor100());
        fullDto.setMark(note.getMark());
        fullDto.setComment(note.getComment());

        return fullDto;
    }


    private NoteFullPlanDto createFullNotePlanDto(Note note){
        NoteFullPlanDto fullDto = new NoteFullPlanDto();

        fullDto.setId(note.getId());
        fullDto.setUserId(note.getUserId());
        fullDto.setMark(note.getMark());
        fullDto.setComment(note.getComment());


        if (note.getEatingPlanId() != null) {
            try {
                EatingPlanDto plan = menuClient.getEatingPlan(note.getEatingPlanId(), note.getUserId());

                fullDto.setType(plan.getType());
                fullDto.setNumberOfPeople(plan.getNumberOfPeople());

                if ("OUTSIDE".equals(plan.getStatus())) {
                    fullDto.setLocation("OUTSIDE");
                } else {
                    fullDto.setLocation("HOME");
                }
            } catch (Exception ex) {
                log.info(ex.getMessage());
            }
        }

        if (note.getRecipeId() != null) {
            try {
                RecipeDto recipe = menuClient.getRecipeById(note.getRecipeId(), note.getUserId());
                fullDto.setRecipeName(recipe.getName());
            } catch (Exception ex) {
                log.info(ex.getMessage());
            }
        }

        return fullDto;
    }
}
