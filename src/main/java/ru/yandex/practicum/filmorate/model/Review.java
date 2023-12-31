package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private Integer reviewId = 0;
    @NotNull
    private String content;
    @NotNull
    private Boolean isPositive;
    @NotNull
    private Integer userId;
    @NotNull
    private Integer filmId;
    private int useful = 0;
    @ToString.Exclude
    private Map<Integer, Boolean> likeReviewList = new HashMap<>(); //список оценивших отзыв и их оценка(полезно или нет)
}