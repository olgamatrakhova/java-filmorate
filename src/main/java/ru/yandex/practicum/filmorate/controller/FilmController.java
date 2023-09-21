package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/films")
    public List<Film> getFilms() {
        log.info("Запрос на получение списка фильмов (getFilms())");
        return filmService.filmStorage.getFilms();
    }

    @GetMapping("/films/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        log.info("Запрос на получение фильма по id (getFilmById(" + id + "))");
        return filmService.filmStorage.getFilmById(id);
    }

    @PostMapping("/films")
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление фильмов (addFilm(" + film + "))");
        return filmService.filmStorage.addFilm(film);
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на изменения фильма (updateFilm(" + film + "))");
        return filmService.filmStorage.updateFilm(film);
    }

    @DeleteMapping("/films/{id}")
    public void deleteFilmById(@PathVariable Integer id) {
        log.info("Запрос на удаление фильма (deleteFilmById(" + id + "))");
        filmService.filmStorage.deleteFilmById(id);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void setLikeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Запрос на добавление лайка фильму c id = " + id + " от пользователя c id = " + userId + " (setLikeFilm(" + id + "," + userId + "))");
        filmService.setLikeFilm(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void unsetLikeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Запрос на удаление лайка фильму c id = " + id + " от пользователя c id = " + userId + " (unsetLikeFilm(" + id + "," + userId + "))");
        filmService.unsetLikeFilm(id, userId);
    }

    @GetMapping("/films/popular")
    public List<Film> getPopularFilms(@RequestParam(name = "count", defaultValue = "10") Integer count) {
        log.info("Запрос самых популярных фильмов в количестве: " + count + " (getPopularFilms(" + count + "))");
        return filmService.getPopularFilms(count).stream().map(id -> {
            try {
                return filmService.filmStorage.getFilmById(id);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}