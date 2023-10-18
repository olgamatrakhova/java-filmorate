package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmDbStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserDbStorage userStorage;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Integer id) {
        return filmStorage.getFilmById(id);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void deleteFilmById(Integer id) {
        filmStorage.deleteFilmById(id);
    }

    public void setLikeFilm(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film != null) {
            User user = userStorage.getUserById(userId);
            if (user != null) {
                filmStorage.setLikeFilm(filmId, userId);
            } else {
                throw new NotFoundException("Нет пользователя а с id = " + userId);
            }
        } else {
            throw new NotFoundException("Нет фильма с id = " + filmId);
        }
    }

    public void unsetLikeFilm(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film != null) {
            User user = userStorage.getUserById(userId);
            if (user != null) {
                filmStorage.unsetLikeFilm(filmId, userId);
            } else {
                throw new NotFoundException("Нет пользователя а с id = " + userId);
            }
        } else {
            throw new NotFoundException("Нет фильма с id = " + filmId);
        }
    }

    public List<Film> getPopularFilms(int count) {
        if (count < 1) {
            throw new NotFoundException("Count должен быть больше или равен 1. Ваше значение: " + count);
        }
        return filmStorage.getPopularFilms(count);
    }
}