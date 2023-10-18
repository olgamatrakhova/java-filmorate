package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.MpaDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    @Qualifier("mpaDbStorage")
    private final MpaDbStorage mpaStorage;

    public List<Mpa> getMpa() {
        return mpaStorage.getMpa();
    }

    public Mpa getMpaById(int id) throws NotFoundException {
        return mpaStorage.getMpaById(id);
    }
}