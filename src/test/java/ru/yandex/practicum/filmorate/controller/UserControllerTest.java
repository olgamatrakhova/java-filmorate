package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    protected void init() {
        testUser = User.builder()
                .email("mail@mail.ru")
                .login("testlogin")
                .name("Test Name")
                .birthday(LocalDate.of(1986, 07, 26))
                .build();
    }

    @Test
    @Order(1)
    void addUser_correctUser_OkTest() throws Exception {
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void addUser_blankName_OkTest() throws Exception {
        testUser.setName("");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.name").value(""));
    }

    @Test
    void addUser_badEmail_badRequestTest() throws Exception {
        testUser.setEmail("badEmail.ru");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_blankLogin_badRequestTest() throws Exception {
        testUser.setLogin("");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_futureBirthdayDate_badRequestTest() throws Exception {
        testUser.setBirthday(LocalDate.parse("2028-07-26"));
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }
}