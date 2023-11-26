package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {

    List<Director> getDirectors();

    Director addDirector(Director director);

    Director updateDirector(Director director);

    Director getDirectorById(int id);

    void deleteDirectorById(int id);
}