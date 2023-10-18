package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}