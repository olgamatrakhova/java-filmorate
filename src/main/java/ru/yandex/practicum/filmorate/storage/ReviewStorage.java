package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    List<Review> getAllReview();

    List<Review> getAllReviewForFilm(int filmId, int count);

    Review getReviewById(int id);

    Review addReview(Review review);

    Review updateReview(Review review);

    boolean deleteReview(int id);

    Review addLikeFromUser(int id, int userId);

    Review addDislikeFromUser(int id, int userId);

    Review deleteLikeFromUser(int id, int userId);

    Review deleteDislikeFromUser(int id, int userId);
}