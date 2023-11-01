package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private User getRowMapUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getInt("user_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }

    @Override
    public List<User> getUsers() {
        String sql = "select user_id, email, login, name, birthday from users";
        return jdbcTemplate.query(sql, this::getRowMapUser);
    }

    @Override
    public User addUser(User user) {
        String sql = "insert into users(email, login, name, birthday) values(?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        return getUserById(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    @Override
    public User updateUser(User user) {
        String sql = "update users set email = ?, login = ?, name = ?, birthday = ? where user_id = ?";
        int usersUpd = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()), user.getId());
        if (usersUpd == 0) throw new NotFoundException("Не возможно обновить пользователя с id = " + user.getId());
        return getUserById(user.getId());
    }

    @Override
    public User getUserById(int id) {
        String sql = "select user_id, email, login, name, birthday from users where user_id = ?";
        User user;
        try {
            user = jdbcTemplate.queryForObject(sql, this::getRowMapUser, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NotFoundException("Нет пользователя с id = " + id);
        }
        return user;
    }

    @Override
    public void deleteUserById(int id) {
        String sqlDelFriends = "delete from friends where from_user_id = ? or to_user_id = ?";
        String sqlDelLikes = "delete from likes where user_id = ?";
        String sqlDelUser = "delete from users where user_id = ?";
        jdbcTemplate.update(sqlDelLikes, id);
        jdbcTemplate.update(sqlDelFriends, id, id);
        if (jdbcTemplate.update(sqlDelUser, id) == 0)
            throw new NotFoundException("Не возможно удалить пользователя с id = " + id);
    }

    public List<User> getFriends(int userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Нет пользователя с таким id = " + userId);
        }
        String sql = "select u.* from friends f join users u on u.user_id = f.to_user_id where f.from_user_id = ?";
        return jdbcTemplate.query(sql, this::getRowMapUser, userId);
    }

    public void addFriend(int userId, int friendId) {
        String sqlSel = "select (select count(*) from friends f where f.from_user_id = ? and f.to_user_id = ?) c1," +
                "               (select count(*) from friends f where f.from_user_id = ? and f.to_user_id = ?) c2";
        String sqlIns = "insert into friends(from_user_id, to_user_id, status) values(?, ?, ?)";
        String sqlUpd = "update friends set status = ? where from_user_id = ? and to_user_id = ?";
        try {
            Object[] res = jdbcTemplate.queryForList(sqlSel, userId, friendId, friendId, userId).get(0).values().toArray();
            if (res[0].equals((long) 0)) {
                if (!res[1].equals((long) 0)) {
                    jdbcTemplate.update(sqlIns, userId, friendId, true);
                    jdbcTemplate.update(sqlUpd, true, friendId, userId);
                } else {
                    jdbcTemplate.update(sqlIns, userId, friendId, false);
                }
            }
        } catch (
                Exception e) {
            throw new NotFoundException("Нет пользователей с id = " + userId + " или id = " + friendId);
        }
    }

    public void deleteFriends(int userId, int friendId) {
        String sql = "delete from friends where from_user_id = ? and to_user_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "select * from users" +
                "       where user_id in (select f1.to_user_id " +
                "                           from friends f1" +
                "                           left join friends f2 on f1.to_user_id = f2.to_user_id" +
                "                          where f1.from_user_id = ?" +
                "                            and f2.from_user_id = ?)";

        return jdbcTemplate.query(sql, this::getRowMapUser, userId, otherId);
    }
}