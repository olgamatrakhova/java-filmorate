package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    public UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriends(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user != null) {
            if (friend != null) {
                if (user.getFriends() == null) {
                    user.setFriends(new HashSet<>());
                }
                user.getFriends().add(friendId);

                if (friend.getFriends() == null) {
                    friend.setFriends(new HashSet<>());
                }
                friend.getFriends().add(userId);

            } else {
                throw new NotFoundException("Нельзя добавить друга. Нет пользователя с id = " + friendId);
            }
        } else {
            throw new NotFoundException("Нет пользователя с id = " + userId);
        }
    }

    public void deleteFriends(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user != null) {
            if (friend != null) {
                if (user.getFriends() == null) {
                    throw new NotFoundException("Нельзя удалить друга. Нет друзей у пользователя с id = " + userId);
                }
                if (friend.getFriends() == null) {
                    throw new NotFoundException("Нельзя удалить друга. Нет друзей у пользователя с id = " + friendId);
                }
                if (user.getFriends().remove(friendId)) {
                    friend.getFriends().remove(userId);
                } else {
                    throw new NotFoundException("Нельзя удалить друга. Пользователя с id = " + friendId + " нет в друзьях у пользователя с id = " + userId);
                }
            } else {
                throw new NotFoundException("Нельзя удалить друга. Нет пользователя с id = " + friendId);
            }
        } else {
            throw new NotFoundException("Нет пользователя с id = " + userId);
        }
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getUserById(userId);
        if (user != null) {
            if (user.getFriends().size() == 0) {
                throw new NotFoundException("Нет друзей у пользователя с id = " + userId);
            }
        } else {
            throw new NotFoundException("Нет пользователя с id = " + userId);
        }
        return user.getFriends().stream().map(u -> userStorage.getUserById(u)).collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherUserId);
        List<User> result = new ArrayList<>();
        Set<Integer> commonFriends = null;
        if (user != null) {
            if (otherUser != null) {
                if (user.getFriends() != null && otherUser.getFriends() != null) {
                    commonFriends = new HashSet<>(user.getFriends());
                    commonFriends.retainAll(otherUser.getFriends());
                    result = commonFriends.stream().map(x -> userStorage.getUserById(x)).collect(Collectors.toList());
                }
            } else {
                throw new NotFoundException("Нет пользователя с id = " + otherUserId);
            }
        } else {
            throw new NotFoundException("Нет пользователя с id = " + userId);
        }
        return result;
    }
}