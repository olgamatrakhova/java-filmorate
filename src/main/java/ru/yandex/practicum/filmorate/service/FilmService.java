package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmDbStorage filmStorage;

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
        filmStorage.setLikeFilm(filmId, userId);
    }

    public void unsetLikeFilm(int filmId, int userId) {
        filmStorage.unsetLikeFilm(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count < 1) {
            throw new NotFoundException("Count должен быть больше или равен 1. Ваше значение: " + count);
        }
        return filmStorage.getPopularFilms(count);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getDirectorFilmsSort(int directorId, String sortBy) {
        return filmStorage.getDirectorFilmsSort(directorId, sortBy);
    }

    public List<Film> searchFilms(String query, List<String> by) {
        if (by.size() == 2) {
            List<Film> films = filmStorage.searchByDirector(query);
            films.addAll(filmStorage.searchByTitle(query));
            return films;
        } else {
            switch (by.get(0)) {
                case "director":
                    return filmStorage.searchByDirector(query);
                case "title":
                    return filmStorage.searchByTitle(query);
                default:
                    throw new NotFoundException("Ошибка поиска. Некорректный параметр = " + by.get(0));
            }
        }
    }

    public List<Film> getPopularFilmsByGenreAndYear(int limit, Optional<Long> genreId, Optional<Long> year) {
        List<Film> films;

        if (genreId.isEmpty() && year.isEmpty()) {
            films =  getPopularFilms(limit);
        } else if (genreId.isPresent() && year.isEmpty()) {
            films =  filmStorage.getPopularFilmsByGenre(limit, genreId.get());
        } else if (genreId.isEmpty()) {
            films =  filmStorage.getPopularFilmsByYear(limit, year.get());
        } else {
            films =  filmStorage.getPopularFilmsByGenreAndYear(limit, genreId.get(), year.get());
        }
        return films;
    }

}