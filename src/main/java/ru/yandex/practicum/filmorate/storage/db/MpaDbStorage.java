package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    private Mpa rowMapMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("mpa_id"), rs.getString("name"));
    }

    @Override
    public List<Mpa> getMpa() {
        String sql = "select mpa_id, name from mpa order by mpa_id";
        return jdbcTemplate.query(sql, this::rowMapMpa);
    }

    @Override
    public Mpa getMpaById(int id) {
        String sql = "select mpa_id, name from mpa where mpa_id = ?";
        Mpa mpa;
        try {
            mpa = jdbcTemplate.queryForObject(sql, this::rowMapMpa, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Нет рейтинга с id= " + id);
        }
        return mpa;
    }
}