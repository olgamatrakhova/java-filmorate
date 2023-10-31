package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDirector;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userStorage;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

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
                .directors(null)
                .build();
    }

    private FilmGenre getRowMapFilmGenre(ResultSet rs, int rowNum) throws SQLException {
        return new FilmGenre(rs.getInt("film_id"), new Genre(rs.getInt("genre_id"), rs.getString("name")));
    }

    private FilmDirector getRowMapFilmDirector(ResultSet rs, int rowNum) throws SQLException {
        return new FilmDirector(rs.getInt("film_id"), new Director(rs.getInt("director_id"), rs.getString("name")));
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
        addFilmDirector(filmId, film);
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
        addFilmDirector(film.getId(), film);
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
            films = getFilmDirector(films);
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

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = "SELECT f.*, m.name AS mpa_name " +
                " FROM (SELECT FILM_ID " +
                " FROM (	SELECT * " +
                "FROM LIKES AS l " +
                " WHERE l.USER_ID = ? " +
                " UNION " +
                " SELECT * " +
                " FROM LIKES AS l2 " +
                " WHERE l2.USER_ID = ? " +
                " ) AS c " +
                " GROUP BY c.FILM_ID " +
                " HAVING COUNT(FILM_ID) = 2) AS c " +
                " LEFT JOIN FILMS AS f ON c.film_id = f.FILM_ID " +
                " LEFT JOIN  MPA AS m ON f.MPA_ID = m.MPA_ID " +
                " LEFT JOIN LIKES AS l3 ON f.film_id = l3.FILM_ID " +
                " GROUP BY l3.FILM_ID " +
                " ORDER BY COUNT(USER_ID) DESC ";

        return getFilmsGenre(jdbcTemplate.query(sql, this::getRowMapFilm, userId, friendId));
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
        List<Film> resultFilms = new ArrayList<>();
        for (Film film : films) {
            List<Genre> genres = new ArrayList<>();
            for (FilmGenre filmGenre : filmGenres) {
                if (film.getId() == filmGenre.getId()) {
                    genres.add(filmGenre.getGenres());
                }
            }
            film.setGenres(genres);
            setUsersLikes(film != null ? film.getId() : 0, film);
            resultFilms.add(film);
        }
        resultFilms = getFilmDirector(resultFilms);
        return resultFilms;
    }

    private void addFilmDirector(int filmId, Film film) {
        String sqlSel = "select count(*) from film_directors where film_id = ? and director_id = ?";
        String sqlIns = "insert into film_directors (film_id, director_id) values(?, ?)";
        String sqlDel = "delete from film_directors where film_id = ?";
        jdbcTemplate.update(sqlDel, filmId);
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                if (jdbcTemplate.queryForObject(sqlSel, Integer.class, filmId, director.getId()) == 0) {
                    try {
                        jdbcTemplate.update(sqlIns, filmId, director.getId());
                    } catch (RuntimeException e) {
                        throw new NotFoundException("Не удалось создать связь фильма c id = " + filmId + " и директора c id = " + director.getId());
                    }
                }
            }
        }
    }

    public List<Film> getFilmDirector(List<Film> films) {
        String sql = "select fd.film_id, d.director_id, d.name\n" +
                "       from film_directors fd\n" +
                "       left join directors d on d.director_id = fd.director_id\n" +
                "     order by fd.director_id, fd.film_id";
        List<FilmDirector> filmDirectors = jdbcTemplate.query(sql, this::getRowMapFilmDirector);
        ArrayList<Film> resultFilms = new ArrayList<>();
        for (Film film : films) {
            List<Director> director = new ArrayList<>();
            for (FilmDirector filmDirector : filmDirectors) {
                if (film.getId() == filmDirector.getId()) {
                    director.add(filmDirector.getDirector());
                }
            }
            film.setDirectors(director);
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

    public Map<Integer, List<Integer>> getAllLikedFilmsId() {
        String sql = "SELECT user_id, film_id FROM likes";
        Map<Integer, List<Integer>> likedFilms = new HashMap<>();
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            Integer userId = rs.getInt("user_id");
            if (!likedFilms.containsKey(userId)) {
                likedFilms.put(userId, new ArrayList<>());
                likedFilms.get(userId).add(rs.getInt("film_id"));
            } else {
                likedFilms.get(userId).add(rs.getInt("film_id"));
            }
            return likedFilms;
        });
        return likedFilms;
    }

    public List<Integer> getFilmsIdLikedByUser(Integer userId) {
        String sql = "SELECT user_id, film_id FROM likes WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("film_id"), userId);
    }

    public List<Film> getRecommendations(List<Integer> recommendedFilmIds) {
        String sql = "select f.*, m.name mpa_name" +
                "        from films f" +
                "        join mpa m on m.mpa_id = f.mpa_id" +
                "     where f.film_id in (:filmsId)" +
                "     order by f.rate desc";
        SqlParameterSource parameters = new MapSqlParameterSource("filmsId", recommendedFilmIds);
        List<Film> recommendedFilms = getFilmsGenre(namedJdbcTemplate.query(sql, parameters, this::getRowMapFilm));
        return recommendedFilms.stream()
                .peek(f -> setUsersLikes(f.getId(), f))
                .collect(Collectors.toList());
    }

    public List<Film> getDirectorFilmsSort(int directorId, String sortBy) {
        String sql = "select f.*," +
                "                m.name mpa_name" +
                "            from films f" +
                "            join mpa m on m.mpa_id = f.mpa_id" +
                "           where film_id in (select film_id" +
                "                               from film_directors" +
                "                              where director_id = ?) ";
        List<Film> filmSort;
        if (sortBy.equals("year")) {
            sql += "order by f.release_dt";
            filmSort = jdbcTemplate.query(sql, this::getRowMapFilm, directorId);
        } else if (sortBy.equals("likes")) {
            sql += "order by f.rate desc";
            filmSort = jdbcTemplate.query(sql, this::getRowMapFilm, directorId);
        } else {
            throw new NotFoundException("Ошибка формата сортировки. SortBy = " + sortBy + " не существует");
        }
        if (filmSort.isEmpty()) {
            throw new NotFoundException("Нет фильмов по режиссеру c id = " + directorId);
        }
        filmSort = getFilmsGenre(filmSort);
        return filmSort;
    }

    public List<Film> searchByDirector(String query) {
        String sqlByDirector = "select f.*, m.name mpa_name" +
                "       from films f" +
                "       join mpa m on m.mpa_id = f.mpa_id" +
                "       where f.film_id in (select fd.film_id" +
                "                           from film_directors fd" +
                "                           join directors d on d.director_id = fd.director_id" +
                "                           where lower(d.name) LIKE CONCAT('%', ? ,'%'))";
        return getFilmsGenre(jdbcTemplate.query(sqlByDirector, this::getRowMapFilm, query.toLowerCase()));
    }

    public List<Film> searchByTitle(String query) {
        String sqlByTitle = "select f.*, m.name mpa_name" +
                "       from films f" +
                "       join mpa m on m.mpa_id = f.mpa_id" +
                "       where lower(f.name) LIKE CONCAT('%', ?, '%')";
        return getFilmsGenre(jdbcTemplate.query(sqlByTitle, this::getRowMapFilm, query.toLowerCase()));
    }
}