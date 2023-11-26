package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Component("directorDbStorage")
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    private Director getRowMapDirector(ResultSet rs, int rowNum) throws SQLException {
        return new Director(rs.getInt("director_id"), rs.getString("name"));
    }

    @Override
    public List<Director> getDirectors() {
        String sql = "select director_id, name from directors order by name";
        return jdbcTemplate.query(sql, this::getRowMapDirector);
    }

    @Override
    public Director addDirector(Director director) {
        String sql = "insert into directors(name) values(?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        return getDirectorById(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "update directors set name = ? where director_id = ?";
        int directorUpd = jdbcTemplate.update(sql, director.getName(), director.getId());
        if (directorUpd == 0)
            throw new NotFoundException("Не возможно обновить режиссера с с id = " + director.getId());
        return getDirectorById(director.getId());
    }

    @Override
    public Director getDirectorById(int id) {
        String sql = "select director_id, name from directors where director_id = ?";
        Director director;
        try {
            director = jdbcTemplate.queryForObject(sql, this::getRowMapDirector, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Нет режиссера с id = " + id);
        }
        return director;
    }

    @Override
    public void deleteDirectorById(int id) {
        String sql1 = "delete from film_directors where director_id = ?";
        String sql2 = "delete from directors where director_id = ?";
        if (jdbcTemplate.update(sql1, id) == 0) {
            throw new NotFoundException("Невозможно удалить режиссера с id = " + id);
        }
        if (jdbcTemplate.update(sql2, id) == 0) {
            throw new NotFoundException("Невозможно удалить режиссера с id = " + id);
        }
    }
}