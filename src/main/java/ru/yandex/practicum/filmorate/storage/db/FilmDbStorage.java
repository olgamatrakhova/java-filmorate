package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userStorage;

    private Film getRowMapFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getInt("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_dt").toLocalDate())
                .duration(rs.getInt("duration"))
                .genres(null)
                .rate(rs.getInt("rate"))
                .mpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")))
                .build();
    }

    private FilmGenre getRowMapFilmGenre(ResultSet rs, int rowNum) throws SQLException {
        return new FilmGenre(rs.getInt("film_id"), new Genre(rs.getInt("genre_id"), rs.getString("name")));
    }

    @Override
    public List<Film> getFilms() {
        String sql = "select f.*, m.name mpa_name" +
                "       from films f" +
                "       join mpa m on m.mpa_id = f.mpa_id";
        return getFilmsGenre(jdbcTemplate.query(sql, this::getRowMapFilm));
    }

    @Override
    public Film addFilm(Film film) {
        String sql = "insert into films(name, description, release_dt, duration, rate,mpa_id) values(?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getRate());
            ps.setInt(6, film.getMpa().getId());
            return ps;
        }, keyHolder);
        int filmId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        addFilmGenres(filmId, film);
        setUsersLikes(filmId, film);
        return getFilmById(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "update films set name = ?, description = ?, release_dt = ?, duration = ?, rate = ?, mpa_id = ? where film_id = ?";
        int filmUpd = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getRate(),
                film.getMpa().getId(),
                film.getId());
        if (filmUpd == 0) {
            throw new NotFoundException("Не удалось обновить фильм с id = " + film.getId());
        }
        addFilmGenres(film.getId(), film);
        return getFilmById(film.getId());
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "select f.*, m.name mpa_name" +
                "       from films f" +
                "       join mpa m on m.mpa_id = f.mpa_id" +
                "      where f.film_id = ?";
        List<Film> films = new ArrayList<>();
        Film film;
        try {
            film = jdbcTemplate.queryForObject(sql, this::getRowMapFilm, id);
            films.add(film);
            films = getFilmsGenre(films);
            if (films != null) film = films.get(0);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Нет фильма с id = " + id);
        }
        setUsersLikes(film != null ? film.getId() : 0, film);
        return film;
    }

    @Override
    public void deleteFilmById(int id) {
        String sql = "delete from films where film_id = ?";
        if (jdbcTemplate.update(sql, id) == 0) {
            throw new NotFoundException("Невозможно удалить фильм с id = " + id);
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "select f.*, m.name mpa_name" +
                "        from films f" +
                "        join mpa m on m.mpa_id = f.mpa_id" +
                "     order by f.rate desc" +
                "     limit ?";
        return getFilmsGenre(jdbcTemplate.query(sql, this::getRowMapFilm, count));
    }

    private void addFilmGenres(int filmId, Film film) {
        String sqlSel = "select count(*) from film_genres where film_id = ? and genre_id = ?";
        String sqlIns = "insert into film_genres (film_id, genre_id) values(?, ?)";
        String sqlDel = "delete from film_genres where film_id = ?";
        jdbcTemplate.update(sqlDel, filmId);
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (jdbcTemplate.queryForObject(sqlSel, Integer.class, filmId, genre.getId()) == 0) {
                    try {
                        jdbcTemplate.update(sqlIns, filmId, genre.getId());
                    } catch (RuntimeException e) {
                        throw new NotFoundException("Не удалось создать связь фильма c id = " + filmId + " и жанра c id = " + genre.getId());
                    }
                }
            }
        }
    }

    public List<Film> getFilmsGenre(List<Film> films) {
        String sql = "select fg.film_id, g.genre_id, g.name\n" +
                "       from film_genres fg\n" +
                "       left join genres g on g.genre_id = fg.genre_id\n" +
                "     order by fg.film_id";
        List<FilmGenre> filmGenres = jdbcTemplate.query(sql, this::getRowMapFilmGenre);
        ArrayList<Film> resultFilms = new ArrayList<>();
        for (Film film : films) {
            List<Genre> genres = new ArrayList<>();
            for (FilmGenre filmGenre : filmGenres) {
                if (film.getId() == filmGenre.getId()) {
                    genres.add(filmGenre.getGenres());
                }
            }
            film.setGenres(genres);
            resultFilms.add(film);
        }
        return resultFilms;
    }

    private void setUsersLikes(Integer id, Film film) {
        String sql = "select user_id from likes where film_id = ?";
        Set<Integer> usersIdsLiked = new HashSet<>();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        while (rowSet.next()) {
            usersIdsLiked.add(rowSet.getInt("user_id"));
        }
        film.setLikes(usersIdsLiked);
    }

    public void setLikeFilm(Integer filmId, Integer userId) throws NotFoundException {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        String sqlIns = "insert into likes(film_id, user_id) values(?, ?)";
        String sqlUpd = "update films set rate = rate + 1 where film_id = ?";
        jdbcTemplate.update(sqlIns, film.getId(), user.getId());
        jdbcTemplate.update(sqlUpd, film.getId());
    }

    public void unsetLikeFilm(Integer filmId, Integer userId) throws NotFoundException {
        String sqlDel = "delete from likes where film_id = ? and user_id = ?";
        String sqlSel = "select rate from films where film_id = ?";
        String sqlUpd = "update films set rate = ? where film_id = ?";
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        jdbcTemplate.update(sqlDel, film.getId(), user.getId());
        try {
            int rate = jdbcTemplate.queryForObject(sqlSel, Integer.class, film.getId());
            if (rate > 0)
                rate--;
            jdbcTemplate.update(sqlUpd, rate, film.getId());
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Не возможно убрать лайк.");
        }
    }
}