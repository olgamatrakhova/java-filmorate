package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        log.info("Запрос на получение списка пользователей (getUsers())");
        return userService.userStorage.getUsers();
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Integer id) {
        log.info("Запрос на получение пользователя по id (getUserById(" + id + "))");
        return userService.userStorage.getUserById(id);
    }

    @PostMapping("/users")
    public User addUser(@Valid @RequestBody User user) {
        log.info("Запрос на добавление пользователя (addUser(" + user + "))");
        return userService.userStorage.addUser(user);
    }

    @PutMapping("/users")
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Запрос на изменения пользователя (updateUser(" + user + "))");
        return userService.userStorage.updateUser(user);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUserById(@PathVariable Integer id) {
        log.info("Запрос на удаление пользователя (deleteUserById(" + id + "))");
        userService.userStorage.deleteUserById(id);
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public void addFriends(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Запрос на добавление в друзья пользователя с id = " + id + " и id = " + friendId + " (addFriends(" + id + "," + friendId + "))");
        userService.addFriends(id, friendId);
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public void deleteFriends(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Запрос на удаление из друзей пользователя с id = " + id + " и id = " + friendId + " (deleteFriends(" + id + "," + friendId + "))");
        userService.deleteFriends(id, friendId);
    }

    @GetMapping("/users/{id}/friends")
    public List<User> getFriends(@PathVariable Integer id) {
        log.info("Запрос списка друзей пользователя (getFriends(" + id + "))");
        return userService.getFriends(id);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        log.info("Запрос на получение списка общих друзей пользователя с id = " + id + " и id = " + otherId + " (getCommonFriends(" + id + "," + otherId + "))");
        return userService.getCommonFriends(id, otherId);
    }
}