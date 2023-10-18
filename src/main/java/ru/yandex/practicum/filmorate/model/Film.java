package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import ru.yandex.practicum.filmorate.validation.ValidateReleaseDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class Film {
    private int id;
    @NonNull
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    @Past
    @ValidateReleaseDate(message = "")
    private LocalDate releaseDate;
    @Positive
    private int duration;
    private Set<Integer> likes;
    private int rate;
    private List<Genre> genres;
    @NonNull
    private Mpa mpa;
}