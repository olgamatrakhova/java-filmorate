package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

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
                if (film.getLikes() == null) {
                    film.setLikes(new HashSet<>());
                }
                film.getLikes().add(userId);
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
                if (!film.getLikes().remove(userId)) {
                    throw new NotFoundException("Пользователь с id = " + userId + " лайк не ставил");
                }
            } else {
                throw new NotFoundException("Нет пользователя а с id = " + userId);
            }
        } else {
            throw new NotFoundException("Нет фильма с id = " + filmId);
        }
    }

    public List<Integer> getPopularFilms(int count) {
        if (count < 1) {
            throw new NotFoundException("Count должен быть больше или равен 1. Ваше значение: " + count);
        }
        return filmStorage.getFilms().stream().sorted((a, b) -> {
            try {
                return getLikesCount(b.getId()) - getLikesCount(a.getId());
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }).limit(count).map(Film::getId).collect(Collectors.toList());
    }

    private int getLikesCount(Integer filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film != null) {
            if (film.getLikes() != null) return film.getLikes().size();
            else return 0;
        } else {
            throw new NotFoundException("Нет фильма с id = " + filmId);
        }
    }
}