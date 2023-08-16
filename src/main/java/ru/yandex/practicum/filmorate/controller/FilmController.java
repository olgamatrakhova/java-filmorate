package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int idFilm = 0;

    @GetMapping
    public List<Film> getFilms() {
        log.info("Запрос на получение списка фильмов.");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        film.setId(getIdFilm());
        films.put(film.getId(), film);
        log.info("Запрос на добавление фильмов. Фильм добавлен.");
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Запрос на изменения фильма. Фильм изменён.");
        } else {
            log.error("Запрос на изменения фильма. Фильм не найден.");
            throw new FilmException("Фильм не найден.");
        }
        return film;
    }

    private int getIdFilm() {
        return ++idFilm;
    }
}