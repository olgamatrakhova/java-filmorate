package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockBean
    private FilmService filmService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addFilm_correctFilm_okTest() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"Test film\",\n" +
                                "  \"description\": \"test description\",\n" +
                                "  \"releaseDate\": \"2000-01-01\",\n" +
                                "  \"duration\": 100,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void addFilm_blankName_badRequestTest() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"\",\n" +
                                "  \"description\": \"test description\",\n" +
                                "  \"releaseDate\": \"2000-01-01\",\n" +
                                "  \"duration\": 100,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    void addFilm_longDescription_badRequestTest() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"Test film\",\n" +
                                "  \"description\": \"Размер описания этого фильма должен превышать двести символов.\" +\n" +
                                "                \"Это очень очень очень очень очень очень очень очень очень очень очень длинное предлинное описание фильма,\" +\n" +
                                "                \"которое должно превышать в описание двести символов. Точно должно превышать!\",\n" +
                                "  \"releaseDate\": \"2000-01-01\",\n" +
                                "  \"duration\": 100,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    void addFilm_futureRealiseDate_badRequestTest() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"Test film\",\n" +
                                "  \"description\": \"test description\",\n" +
                                "  \"releaseDate\": \"2056-01-01\",\n" +
                                "  \"duration\": -100,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    void addFilm_badDuration_badRequestTest() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"Test film\",\n" +
                                "  \"description\": \"test description\",\n" +
                                "  \"releaseDate\": \"2000-01-01\",\n" +
                                "  \"duration\": -100,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    void addFilm_earlyReleaseDate_badRequestTest() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"Test film\",\n" +
                                "  \"description\": \"test description\",\n" +
                                "  \"releaseDate\": \"1000-01-01\",\n" +
                                "  \"duration\": -100,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    void updateFilm_OkTest() throws Exception {
        mockMvc.perform(
                post("/films")
                        .content("{\n" +
                                "  \"name\": \"Test film\",\n" +
                                "  \"description\": \"test description\",\n" +
                                "  \"releaseDate\": \"2000-01-01\",\n" +
                                "  \"duration\": 100,\n" +
                                "  \"mpa\": { \"id\": 1}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(
                put("/films")
                        .content("{\n" +
                                "  \"id\": 1,\n" +
                                "  \"name\": \"Test film2\",\n" +
                                "  \"description\": \"test description\",\n" +
                                "  \"releaseDate\": \"2000-01-01\",\n" +
                                "  \"duration\": 100,\n" +
                                "  \"rate\": 4,\n" +
                                "  \"mpa\": { \"id\": 2}\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void getPopularFilmsByMarks_thenResponseStatusOk_WithListOfSortedFilmsByMarksInBody() throws Exception {
        Film film1 = Film.builder()
                .name("Film1")
                .description("Film1 Description")
                .releaseDate(LocalDate.of(1989, 12, 28))
                .duration(120)
                .mpa(new Mpa(3, "PG-13"))
                .genres(List.of(new Genre(2, "Drama")))
                .rate(0)
                .rateByMarks(5)
                .build();
        film1.setId(1);

        Film film2 = Film.builder()
                .name("Film2")
                .description("Film2 Description")
                .releaseDate(LocalDate.of(1999, 9, 3))
                .duration(80)
                .mpa(new Mpa(1, "G"))
                .genres(List.of(new Genre(4, "Thriller")))
                .rate(0)
                .rateByMarks(9.5)
                .build();
        film2.setId(2);

        List<Film> films = new java.util.ArrayList<>(List.of(film1, film2));
        films.sort(Comparator.comparing(Film::getRateByMarks).reversed());
        when(filmService.getPopularMarkedFilmsByGenreAndYear(10, Optional.empty(), Optional.empty()))
                .thenReturn(films);

        String response = mockMvc.perform(get("/films/popularByMarks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(films)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(filmService).getPopularMarkedFilmsByGenreAndYear(10, Optional.empty(), Optional.empty());
        assertEquals(objectMapper.writeValueAsString(films), response);
    }

    @Test
    void getDirectorFilmsSortByMark_thenResponseStatusOk_WithListOfSortedFilmsByMarksInBody() throws Exception {
        Film film1 = Film.builder()
                .name("Film1")
                .description("Film1 Description")
                .releaseDate(LocalDate.of(1989, 12, 28))
                .duration(120)
                .mpa(new Mpa(3, "PG-13"))
                .genres(List.of(new Genre(2, "Drama")))
                .rate(0)
                .rateByMarks(5)
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
                .rateByMarks(5)
                .directors(List.of(new Director(1,"Director1")))
                .build();
        film2.setId(2);

        List<Film> films = new java.util.ArrayList<>(List.of(film1, film2));
        films.sort(Comparator.comparing(Film::getRateByMarks).reversed());
        when(filmService.getDirectorFilmsSortByMark(1, "marks"))
                .thenReturn(films);

        String response = mockMvc.perform(get("/films/directorByMark/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(films)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(filmService).getDirectorFilmsSortByMark(1, "marks");
        assertEquals(objectMapper.writeValueAsString(films), response);
    }

    @Test
    void markFilm_thenResponseStatusOk() throws Exception {
        mockMvc.perform(put("/films/1/marks/?userId=1&mark=10"))
                .andExpect(status().isOk());
    }
}