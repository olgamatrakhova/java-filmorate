package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.db.DirectorDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorStorage;

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director getDirectorById(int id) {
        return directorStorage.getDirectorById(id);
    }

    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public void deleteDirectorById(int id) {
        directorStorage.deleteDirectorById(id);
    }
}