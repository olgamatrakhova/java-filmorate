package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FilmController.class)
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Film testFilm;

    @BeforeEach
    protected void init() {
        testFilm = Film.builder()
                .name("Тестовый фильм")
                .description("Тестовое описание тестового фильма")
                .releaseDate(LocalDate.of(1995, 11, 18))
                .duration(95)
                .build();
    }

    @Test
    void createNewCorrectFilmOkTest() throws Exception {
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(testFilm))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void createNewBlankNameFilmBadTest() throws Exception {
        testFilm.setName("");
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(testFilm))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNewLongDescriptionFilmBadTest() throws Exception {
        testFilm.setDescription("Размер описания этого фильма должен превышать двести символов." +
                "Это очень очень очень очень очень очень очень очень очень очень очень длинное предлинное описание фильма," +
                "которое должно превышать в описание двести символов. Точно должно превышать!");
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(testFilm))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNewFutureDateFilmBadTest() throws Exception {
        testFilm.setReleaseDate(LocalDate.of(2036, 9, 19));
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(testFilm))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNewVeryEarlyFilmDateBadTest() throws Exception {
        testFilm.setReleaseDate(LocalDate.of(1885, 12, 12));
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(testFilm))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }
}