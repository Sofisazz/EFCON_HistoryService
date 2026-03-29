package com.example.historyservice.service.V1.serviceImpl;

import com.example.historyservice.dto.*;
import com.example.historyservice.dto.mapping.NoteMapper;
import com.example.historyservice.entity.Note;
import com.example.historyservice.exceptions.MissingException;
import com.example.historyservice.repository.NoteRepository;
import com.example.historyservice.service.V1.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    @Override
    public Page<NoteDto> findAllRecipes(Pageable pageable) {
        return noteRepository.findAll(pageable).map(noteMapper::toDto);
    }

    @Override
    public NoteDto findNoteById(int id) {
        return noteRepository.findById(id).map(noteMapper::toDto)
                .orElseThrow(() -> new MissingException("Запись id '" + id + "' не найдена"));
    }

    @Transactional
    @Override
    public NoteDto createNote(NoteDto noteDto) {

        return noteMapper.toDto(noteRepository.save(noteMapper.toEntity(noteDto)));
    }

    @Transactional
    @Override
    public NoteDto updateNoteById(int id, NoteDto noteDto) {
        Note receivedNote = noteRepository.findById(id)
                .orElseThrow(() -> new MissingException("Запись id '" + id + "' не найдена"));

        noteMapper.updateFromDto(noteDto, receivedNote);
        noteRepository.save(receivedNote);

        return noteMapper.toDto(receivedNote);
    }

    @Transactional
    @Override
    public void deleteNoteById(int id) {
        if (!noteRepository.existsById(id)) {
            throw new MissingException("Запись id '" + id + "' не найдена");
        }

        noteRepository.deleteById(id);
    }
}
