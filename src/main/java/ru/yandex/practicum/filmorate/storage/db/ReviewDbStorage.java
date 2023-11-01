package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component("ReviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Review> getAllReview() {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC";
        return (getReviewListFromRowSet(jdbcTemplate.queryForRowSet(sql)));
    }

    @Override
    public List<Review> getAllReviewForFilm(int filmId, int count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return (getReviewListFromRowSet(jdbcTemplate.queryForRowSet(sql, filmId, count)));
    }

    @Override
    public Review getReviewById(int id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::getRowMapReview, id);
        } catch (RuntimeException e) {
            throw new NotFoundException("Не найден отзыв с id = " + id);
        }
    }

    @Override
    public Review addReview(Review review) {
        int userId = review.getUserId();
        int filmId = review.getFilmId();
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, 0)";
        try {
            jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), userId, filmId);
        } catch (RuntimeException e) {
            throw new NotFoundException("Не удалось добавить отзыв пользователя с id = " + userId + " для фильма с id = " + filmId);
        }
        return getReviewById(getIdLastAddReview(review));
    }

    @Override
    public Review updateReview(Review review) {
        int idReview = review.getReviewId();
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ? ";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), idReview);
        return getReviewById(idReview);
    }

    @Override
    public boolean deleteReview(int id) {
        String sqlDeleteReview = "DELETE FROM reviews WHERE review_id = ?";
        String sqlDeleteLikeForReview = "DELETE FROM likes_review WHERE review_id = ?";
        getReviewById(id);
        try {
            jdbcTemplate.update(sqlDeleteLikeForReview, id);
            if (jdbcTemplate.update(sqlDeleteReview, id) == 0) {
                throw new NotFoundException("Не удалось удалить отзыв " + id);
            }
            return true;
        } catch (RuntimeException e) {
            throw new NotFoundException("Не найден указанный отзыв");
        }
    }

    @Override
    public Review addLikeFromUser(int id, int userId) {
        getReviewById(id);
        String sql = "INSERT INTO likes_review (review_id, user_id, is_useful) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, id, userId, true);
            jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE review_id = ?", id);
        } catch (RuntimeException e) {
            throw new NotFoundException("Не удалось поставить лайк от пользователя id = " + userId + ", для отзыва с id = " + id);
        }
        return getReviewById(id);
    }

    @Override
    public Review addDislikeFromUser(int id, int userId) {
        String sql = "INSERT INTO likes_review (review_id, user_id, is_useful) VALUES (?, ?, ?) ";
        try {
            jdbcTemplate.update(sql, id, userId, false);
            jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE review_id = ?", id);
        } catch (RuntimeException e) {
            throw new NotFoundException("Не удалось поставить дислайк от пользователя id = " + userId + ", для отзыва с id = " + id);
        }
        return getReviewById(id);
    }

    @Override
    public Review deleteLikeFromUser(int id, int userId) {
        String sqlDelete = "DELETE FROM likes_review WHERE review_id = ? AND user_id = ? AND is_useful = true LIMIT 1  ";
        String sqlUpdateUseful = "UPDATE reviews SET useful = ? WHERE review_id = ?";
        try {
            if (jdbcTemplate.update(sqlDelete, id, userId) == 0) {
                throw new NotFoundException("Не удалось удалить лайк пользователя " + userId + " для фильма " + id);
            }
            int count = getUsefulCount(id, userId);
            jdbcTemplate.update(sqlUpdateUseful, count, id);
        } catch (RuntimeException e) {
            throw new NotFoundException("Не найдено лайка от пользователя id = " + userId + ", для отзыва с id = " + id);
        }
        return getReviewById(id);
    }

    @Override
    public Review deleteDislikeFromUser(int id, int userId) {
        String sqlDelete = "DELETE FROM likes_review WHERE review_id = ? AND user_id = ? AND is_useful = false LIMIT 1  ";
        String sqlUpdateUseful = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
        try {
            if (jdbcTemplate.update(sqlDelete, id, userId) == 0) {
                throw new NotFoundException("Не удалось удалить дислайк пользователя " + userId + " для фильма " + id);
            }
            jdbcTemplate.update(sqlUpdateUseful, id);
        } catch (RuntimeException e) {
            throw new NotFoundException("Не найдено дизлайка от пользователя id = " + userId + ", для отзыва с id = " + id);
        }
        return getReviewById(id);
    }

    private Review getRowMapReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getInt("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("film_id"))
                .useful(rs.getInt("useful"))
                .likeReviewList(getLikeForReviewMap(rs.getInt("review_id")))
                .build();
    }

    private List<Review> getReviewListFromRowSet(SqlRowSet rs) {
        Map<Integer, Map<Integer, Boolean>> resultLikeForReview = getAllLikeForReviewMap();

        List<Review> result = new ArrayList<>();
        while (rs.next()) {
            int reviewId = rs.getInt("review_id");
            Map<Integer, Boolean> likeList = new HashMap<>();
            if (resultLikeForReview.containsKey(reviewId)) {
                likeList = resultLikeForReview.get(reviewId);
            }
            result.add(Review.builder()
                    .reviewId(reviewId)
                    .content(rs.getString("content"))
                    .isPositive(rs.getBoolean("is_positive"))
                    .userId(rs.getInt("user_id"))
                    .filmId(rs.getInt("film_id"))
                    .useful(rs.getInt("useful"))
                    .likeReviewList(likeList)
                    .build());
        }
        return result;
    }

    private Map<Integer, Map<Integer, Boolean>> getAllLikeForReviewMap() {
        String sql = "SELECT * FROM likes_review ORDER BY review_id ASC ";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
        Map<Integer, Map<Integer, Boolean>> resultLikeForReview = new HashMap<>();
        Map<Integer, Boolean> reviewLikes = new HashMap<>();
        int lastReviewId = 0;

        while (rowSet.next()) {
            int reviewId = rowSet.getInt("review_id");
            int userId = rowSet.getInt("user_id");
            boolean useful = rowSet.getBoolean("is_useful");

            if (lastReviewId == 0) {
                lastReviewId = reviewId;
            }
            if (lastReviewId != reviewId) {
                resultLikeForReview.put(lastReviewId, reviewLikes);
                reviewLikes = new HashMap<>();
                lastReviewId = reviewId;
            }
            reviewLikes.put(userId, useful);
        }
        resultLikeForReview.put(lastReviewId, reviewLikes);
        return resultLikeForReview;
    }

    private Map<Integer, Boolean> getLikeForReviewMap(int id) {
        String sql1 = "SELECT * FROM likes_review WHERE review_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql1, id);
        Map<Integer, Boolean> result = new HashMap<>();

        while (rowSet.next()) {
            int userId = rowSet.getInt("user_id");
            boolean useful = rowSet.getBoolean("is_useful");
            result.put(userId, useful);
        }
        return result;
    }


    private int getIdLastAddReview(Review review) {
        String sql = " SELECT review_id FROM reviews " +
                " WHERE user_id = ? AND film_id = ? " +
                " ORDER BY review_id DESC " +
                " LIMIT 1 ";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, review.getUserId(), review.getFilmId());
        if (rs.next()) {
            return rs.getInt("review_id");
        } else {
            throw new NotFoundException("Не удалось найти отзыв");
        }
    }

    private int getUsefulCount(int id, int userId) {
        String sql = "SELECT * FROM likes_review WHERE review_id = ? AND user_id = ? ";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id, userId);
        int count = 0;
        while (rs.next()) {
            if (rs.getBoolean("is_useful")) {
                count++;
            } else {
                count--;
            }
        }
        return count;
    }
}
