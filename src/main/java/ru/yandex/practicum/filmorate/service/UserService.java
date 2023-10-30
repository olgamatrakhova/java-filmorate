package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("userDbStorage")
    private final UserDbStorage userStorage;
    @Qualifier("filmDbStorage")
    private final FilmDbStorage filmStorage;

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Integer id) {
        return userStorage.getUserById(id);
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void deleteUserById(Integer id) {
        userStorage.deleteUserById(id);
    }

    public void addFriends(int userId, int friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriends(int userId, int friendId) {
        userStorage.deleteFriends(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }

    public List<Film> getRecommendations(Integer userId) {
        Map<Integer, List<Integer>> allLikedFilms = filmStorage.getAllLikedFilmsIdByUsers();
        allLikedFilms.remove(userId);
        List<Integer> filmsLikedByUser = filmStorage.getFilmsIdLikedByUser(userId);
        int recommendedUserId = 0;
        int maxOfIntersections = 0;
        for (Map.Entry<Integer, List<Integer>> e : allLikedFilms.entrySet()) {
            List<Integer> intersection = e.getValue().stream()
                    .filter(filmsLikedByUser::contains)
                    .collect(Collectors.toList());
            if(intersection.size() > maxOfIntersections) {
                maxOfIntersections = intersection.size();
                recommendedUserId = e.getKey();
            }
        }
        List<Integer> filmsLikedByRecommendedUser = allLikedFilms.get(recommendedUserId);
        List<Integer> recommendedFilmIds = new ArrayList<>();
        if (filmsLikedByRecommendedUser != null) {
            recommendedFilmIds.addAll(filmsLikedByRecommendedUser);
            recommendedFilmIds.removeAll(filmsLikedByUser);
        }
        return filmStorage.getRecommendations(recommendedFilmIds);
    }
}