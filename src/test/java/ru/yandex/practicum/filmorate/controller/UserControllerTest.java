package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void before() {
        jdbcTemplate.update("DROP TABLE IF EXISTS likes CASCADE;\n" +
                "\n" +
                "DROP TABLE IF EXISTS friends CASCADE;\n" +
                "\n" +
                "DROP TABLE IF EXISTS users CASCADE;\n" +
                "\n" +
                "DROP TABLE IF EXISTS film_genres CASCADE;\n" +
                "\n" +
                "DROP TABLE IF EXISTS genres CASCADE;\n" +
                "\n" +
                "DROP TABLE IF EXISTS film_directors CASCADE;\n" +
                "\n" +
                "DROP TABLE IF EXISTS directors CASCADE;\n" +
                "\n" +
                "DROP TABLE IF EXISTS films CASCADE;\n" +
                "\n" +
                "DROP TABLE IF EXISTS mpa CASCADE;\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS mpa\n" +
                "(\n" +
                "    mpa_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,\n" +
                "    name VARCHAR NOT NULL UNIQUE\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS films\n" +
                "(\n" +
                "    film_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,\n" +
                "    name VARCHAR NOT NULL,\n" +
                "    description VARCHAR(300),\n" +
                "    release_dt DATE,\n" +
                "    duration INTEGER,\n" +
                "    rate INTEGER,\n" +
                "    mpa_id INTEGER REFERENCES mpa (mpa_id)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS genres\n" +
                "(\n" +
                "    genre_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,\n" +
                "    name VARCHAR NOT NULL UNIQUE\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS film_genres\n" +
                "(\n" +
                "    film_id INTEGER NOT NULL REFERENCES films (film_id),\n" +
                "    genre_id INTEGER NOT NULL REFERENCES genres (genre_id)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS users\n" +
                "(\n" +
                "    user_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,\n" +
                "    email VARCHAR NOT NULL UNIQUE,\n" +
                "    login VARCHAR NOT NULL UNIQUE,\n" +
                "    name VARCHAR NOT NULL,\n" +
                "    birthday DATE NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS friends\n" +
                "(\n" +
                "    from_user_id INTEGER NOT NULL REFERENCES users (user_id),\n" +
                "    to_user_id INTEGER NOT NULL REFERENCES users (user_id),\n" +
                "    status BOOLEAN NOT NULL,\n" +
                "    PRIMARY KEY (from_user_id, to_user_id)\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS likes\n" +
                "(\n" +
                "    film_id INTEGER NOT NULL REFERENCES films (film_id),\n" +
                "    user_id INTEGER NOT NULL REFERENCES users (user_id)\n" +
                ");CREATE TABLE  IF NOT EXISTS directors (\n" +
                "  director_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,\n" +
                "  name VARCHAR NOT NULL UNIQUE\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS film_directors\n" +
                "(\n" +
                "    film_id INTEGER NOT NULL REFERENCES films (film_id),\n" +
                "    director_id INTEGER NOT NULL REFERENCES directors (director_id)\n" +
                ");\n" +
                "MERGE INTO mpa (mpa_id, name)\n" +
                "VALUES (1, 'G'),\n" +
                "       (2, 'PG'),\n" +
                "       (3, 'PG-13'),\n" +
                "       (4, 'R'),\n" +
                "       (5, 'NC-17');\n" +
                "\n" +
                "MERGE INTO genres (genre_id, name)\n" +
                "VALUES (1, 'Комедия'),\n" +
                "       (2, 'Драма'),\n" +
                "       (3, 'Мультфильм'),\n" +
                "       (4, 'Триллер'),\n" +
                "       (5, 'Документальный'),\n" +
                "       (6, 'Боевик');");
    }

    @Test
    void addUser_correctUser_OkTest() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content("{\n" +
                                "  \"login\": \"login1\",\n" +
                                "  \"name\": \"name1\",\n" +
                                "  \"email\": \"mail1@mail.ru\",\n" +
                                "  \"birthday\": \"1986-07-26\"\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void addUser_blankName_OkTest() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content("{\n" +
                                "  \"login\": \"login1\",\n" +
                                "  \"email\": \"mail1@mail.ru\",\n" +
                                "  \"birthday\": \"1986-07-26\"\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void addUser_badEmail_badRequestTest() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content("{\n" +
                                "  \"login\": \"login1\",\n" +
                                "  \"name\": \"name1\",\n" +
                                "  \"email\": \"mail1.ru\",\n" +
                                "  \"birthday\": \"1986-07-26\"\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    void addUser_futureBirthdayDate_badRequestTest() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content("{\n" +
                                "  \"login\": \"login1\",\n" +
                                "  \"name\": \"name1\",\n" +
                                "  \"email\": \"mail1@mail.ru\",\n" +
                                "  \"birthday\": \"2986-07-26\"\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is4xxClientError());
    }

    @Test
    void updateUse_OkTest() throws Exception {
        mockMvc.perform(
                post("/users")
                        .content("{\n" +
                                "  \"login\": \"login1\",\n" +
                                "  \"name\": \"name1\",\n" +
                                "  \"email\": \"mail1@mail.ru\",\n" +
                                "  \"birthday\": \"1986-07-26\"\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        );
        mockMvc.perform(
                put("/users")
                        .content("{\n" +
                                "  \"id\": 1,\n" +
                                "  \"login\": \"login2\",\n" +
                                "  \"name\": \"name2\",\n" +
                                "  \"email\": \"mail1@mail.ru\",\n" +
                                "  \"birthday\": \"1986-07-26\"\n" +
                                "}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}