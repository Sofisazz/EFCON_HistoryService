package com.example.historyservice.service.serviceImpl;

import com.example.historyservice.dto.NoteDto;
import com.example.historyservice.dto.mapping.NoteMapper;
import com.example.historyservice.entity.Note;
import com.example.historyservice.exceptions.MarkException;
import com.example.historyservice.exceptions.MissingException;
import com.example.historyservice.repository.NoteRepository;
import com.example.historyservice.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
        checkMark(noteDto);

        return noteMapper.toDto(noteRepository.save(noteMapper.toEntity(noteDto)));
    }

    @Transactional
    @Override
    public NoteDto updateNoteById(int id, NoteDto noteDto) {
        Note receivedNote = noteRepository.findById(id)
                .orElseThrow(() -> new MissingException("Запись id '" + id + "' не найдена"));

        checkMark(noteDto);
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

    private void checkMark(NoteDto noteDto){
        if (noteDto.getMark() % 1 != 0) {
            throw new MarkException("Введите целое число (оценка может быть от 1 до 5 баллов)");
        }
    }
}
