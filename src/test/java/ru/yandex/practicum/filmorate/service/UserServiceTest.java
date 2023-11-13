package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceTest {
    private final UserService userService;
    private final FilmService filmService;
    private final DirectorService directorService;

    @Test
    void getRecommendationsByMarks() {
        directorService.addDirector(new Director(1, "Director1"));
        directorService.addDirector(new Director(2, "Director2"));

        Film film1 = Film.builder()
                .name("Film1")
                .description("Film1 Description")
                .releaseDate(LocalDate.of(1989, 12, 28))
                .duration(120)
                .mpa(new Mpa(3, "PG-13"))
                .genres(List.of(new Genre(2, "Drama")))
                .rate(0)
                .rateByMarks(0)
                .directors(List.of(new Director(1,"Director1")))
                .build();
        film1.setId(1);

        Film film2 = Film.builder()
                .name("Film2")
                .description("Film2 Description")
                .releaseDate(LocalDate.of(2020, 1, 2))
                .duration(120)
                .mpa(new Mpa(3, "PG-13"))
                .genres(List.of(new Genre(2, "Drama")))
                .rate(0)
                .rateByMarks(0)
                .directors(List.of(new Director(1,"Director1")))
                .build();
        film2.setId(2);

        Film film3 = Film.builder()
                .name("Film2")
                .description("Film2 Description")
                .releaseDate(LocalDate.of(2021, 3, 8))
                .duration(67)
                .mpa(new Mpa(3, "PG-13"))
                .genres(List.of(new Genre(2, "Drama")))
                .rate(0)
                .rateByMarks(0)
                .directors(List.of(new Director(2,"Director2")))
                .build();
        film3.setId(3);
        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);

        User user1 = new User(1, "user1login", "user1@gmail.com", "User1",
                LocalDate.of(1990, 1, 1), new HashSet<>());
        User user2 = new User(2, "user2login", "user2@gmail.com", "User2",
                LocalDate.of(1980, 2, 2), new HashSet<>());
        User user3 = new User(3, "user3login", "user3@gmail.com", "User3",
                LocalDate.of(1975, 3, 3), new HashSet<>());
        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);

        filmService.markFilm(1, 1, 10);
        filmService.markFilm(2, 1, 1);
        filmService.markFilm(1, 2, 4);
        filmService.markFilm(2, 2, 9);
        filmService.markFilm(1, 3, 9);
        filmService.markFilm(2, 3, 2);
        filmService.markFilm(3, 3, 8);

        assertEquals(1, userService.getRecommendationsByMarks(1).size());
        assertThat(userService.getRecommendationsByMarks(1).get(0)).hasFieldOrPropertyWithValue("id", 3);
    }
}