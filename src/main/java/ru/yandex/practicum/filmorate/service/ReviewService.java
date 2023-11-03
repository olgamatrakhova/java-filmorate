package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    @Qualifier("ReviewDbStorage")
    private final ReviewStorage reviewStorage;
    private final EventService eventService;

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
        Review reviewCreated = reviewStorage.addReview(checkReview(review));
        eventService.createEvent(reviewCreated.getUserId(), EventType.REVIEW, EventOperation.ADD, reviewCreated.getReviewId());
        return reviewCreated;
    }

    public Review updateReview(Review review) {
        Review reviewUpdated = reviewStorage.updateReview(checkReview(review));
        eventService.createEvent(reviewUpdated.getUserId(), EventType.REVIEW, EventOperation.UPDATE, reviewUpdated.getReviewId());
        return reviewUpdated;
    }

    public void deleteReview(int id) {
        Review review = getReviewById(id);
        boolean reviewResult = reviewStorage.deleteReview(id);
        eventService.createEvent(review.getUserId(), EventType.REVIEW, EventOperation.REMOVE, review.getReviewId());
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