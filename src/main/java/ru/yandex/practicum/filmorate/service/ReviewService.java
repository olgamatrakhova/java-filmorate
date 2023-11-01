package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    @Qualifier("ReviewDbStorage")
    private final ReviewStorage reviewStorage;

    public List<Review> getAllReview() {
        return reviewStorage.getAllReview();
    }

    public List<Review> getReviewByFilmId(int filmId, int count) {
        return reviewStorage.getAllReviewForFilm(filmId, count);
    }

    public Review getReviewById(int id) {
        return reviewStorage.getReviewById(id);
    }

    public Review addReview(Review review) {
        return reviewStorage.addReview(checkReview(review));
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(checkReview(review));
    }

    public boolean deleteReview(int id) {
        return reviewStorage.deleteReview(id);
    }

    public Review addLikeReviewFromUser(int id, int userId) {
        return reviewStorage.addLikeFromUser(id, userId);
    }

    public Review addDislikeReviewFromUser(int id, int userId) {
        return reviewStorage.addDislikeFromUser(id, userId);
    }

    public Review delLikeReviewFromUser(int id, int userId) {
        return reviewStorage.deleteLikeFromUser(id, userId);
    }

    public Review delDislikeReviewFromUser(int id, int userId) {
        return reviewStorage.deleteDislikeFromUser(id, userId);
    }

    private Review checkReview(Review review) {
        if ((review == null) || (review.getUserId() == null) || (review.getFilmId() == null)
                || (review.getContent() == null) || (review.getIsPositive() == null)
                || (review.getUserId() < 0) || (review.getFilmId() < 0)
                || (review.getReviewId() == null)) {
            throw new NotFoundException("Необходимо правильно заполнить отзыв");
        }
        return review;
    }

}
