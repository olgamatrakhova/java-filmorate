package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public List<Review> getReviewByFilmId(@RequestParam(name = "filmId", defaultValue = "-1") int filmId, @RequestParam(name = "count", defaultValue = "10") int count) {
        if (filmId == -1) {
            log.info("Запрос на получение списка всех отзывов");
            return reviewService.getAllReview();
        }
        if (count < 0) {
            throw new ValidationException("Количество запрашиваемых отзывов не может быть отрицательным");
        }
        log.info("Запрос на получение списка отзывов на фильм с id = {}", filmId);
        return reviewService.getReviewByFilmId(filmId, count);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) throws NotFoundException {
        log.info("Запрос на получение отзыва с id = {}", id);
        return reviewService.getReviewById(id);
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Запрос на добавление отзыва");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Запрос на обновление отзыва");
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}") //reviews/{id}
    public boolean deleteReview(@PathVariable int id) throws NotFoundException {
        log.info("Запрос на удаление отзыва с id = {}", id);
        return reviewService.deleteReview(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Review addLikeReviewFromUser(@PathVariable(name = "id") int id, @PathVariable(name = "userId") int userId) {
        log.info("Запрос на добавление лайка отзыву с id = {}, пользователем с id = {}", id, userId);
        return reviewService.addLikeReviewFromUser(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review addDislikeReviewFromUser(@PathVariable(name = "id") int id, @PathVariable(name = "userId") int userId) {
        log.info("Запрос на добавление дизлайка отзыву с id = {}, пользователем с id = {}", id, userId);
        return reviewService.addDislikeReviewFromUser(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review delLikeReviewFromUser(@PathVariable(name = "id") int id, @PathVariable(name = "userId") int userId) {
        log.info("Запрос на удаление лайка у отзыва с id = {}, пользователем с id = {}", id, userId);
        return reviewService.delLikeReviewFromUser(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review delDislikeReviewFromUser(@PathVariable(name = "id") int id, @PathVariable(name = "userId") int userId) {
        log.info("Запрос на удаление дизлайка у отзыва с id = {}, пользователем с id = {}", id, userId);
        return reviewService.delDislikeReviewFromUser(id, userId);
    }
}
