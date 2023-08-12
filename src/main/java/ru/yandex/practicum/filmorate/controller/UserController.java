package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int idUser = 0;

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        userValidation(user);
        user.setId(getIdUser());
        users.put(user.getId(), user);
        log.info("Запрос на добавление пользователя. Пользователь добавлен.");
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (users.get(user.getId()) != null) {
            userValidation(user);
            users.put(user.getId(), user);
            log.info("Запрос на изменения пользователя. Пользователь изменён.");
        } else {
            log.error("Запрос на изменения пользователя. Пользователь не найден.");
            throw new UserException("Пользователь не найден.");
        }
        return user;
    }

    private int getIdUser() {
        return ++idUser;
    }

    private void userValidation(User user) throws ValidationException {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }
}