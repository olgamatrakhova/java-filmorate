package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    private Genre getRowMapGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }

    @Override
    public List<Genre> getGenre() {
        String sql = "select genre_id, name from genres order by genre_id";
        return jdbcTemplate.query(sql, this::getRowMapGenre);
    }

    @Override
    public Genre getGenreById(int id) {
        String sql = "select genre_id, name from genres where genre_id = ?";
        Genre genre;
        try {
            genre = jdbcTemplate.queryForObject(sql, this::getRowMapGenre, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Нет жанра с id = " + id);
        }
        return genre;
    }
}