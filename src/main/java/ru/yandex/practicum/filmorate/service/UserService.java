package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("userDbStorage")
    private final UserDbStorage userStorage;
    @Qualifier("filmDbStorage")
    private final FilmDbStorage filmStorage;
    private final EventService eventService;


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
        eventService.createEvent(userId, EventType.FRIEND, EventOperation.ADD, friendId);
    }

    public void deleteFriends(int userId, int friendId) {
        userStorage.deleteFriends(userId, friendId);
        eventService.createEvent(userId, EventType.FRIEND, EventOperation.REMOVE, friendId);
    }

    public List<User> getFriends(int userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }

    public List<Film> getRecommendations(Integer userId) {
        Map<Integer, List<Integer>> allLikedFilms = filmStorage.getAllLikedFilmsId();
        allLikedFilms.remove(userId);
        List<Integer> filmsLikedByUser = filmStorage.getFilmsIdLikedByUser(userId);
        int recommendedUserId = 0;
        int maxOfIntersections = 0;
        for (Map.Entry<Integer, List<Integer>> e : allLikedFilms.entrySet()) {
            List<Integer> intersection = e.getValue().stream()
                    .filter(filmsLikedByUser::contains)
                    .collect(Collectors.toList());
            if (intersection.size() > maxOfIntersections) {
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

    public List<Film> getRecommendationsByMarks(Integer userId) {
        Map<Integer, Map<Integer,Integer>> allMarkedFilms = filmStorage.getAllMarkedFilms();
        Map<Integer,Integer> userRatings = allMarkedFilms.get(userId);
        Map<Integer, Integer> similarityScores = new HashMap<>();

        for (Map.Entry<Integer, Map<Integer, Integer>> entry : allMarkedFilms.entrySet()) {
            int otherUser = entry.getKey();
            if (otherUser != userId) {
                Map<Integer, Integer> otherUserRatings = entry.getValue();
                int similarityScore = calculateSimilarity(userRatings, otherUserRatings);
                similarityScores.put(otherUser, similarityScore);
            }
        }

        int usersToRecommend = 1; // кол-во пользователей с похожими оценками, на основе которых даются рекомендации
        List<Integer> similarUsers = similarityScores.entrySet().stream() // пользователи отсортированы по убыванию схожести
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .limit(usersToRecommend)
                .collect(Collectors.toList());

        Set<Integer> moviesOfSimilarUsers = getMoviesOfSimilarUsers(allMarkedFilms, similarUsers);
        List<Integer> recommendedFilms = new ArrayList<>(moviesOfSimilarUsers);
        recommendedFilms.removeAll(userRatings.keySet());  // Исключены фильмы, которые пользователь уже оценил
        return filmStorage.getRecommendationsByMarks(recommendedFilms);
    }

    private int calculateSimilarity(Map<Integer, Integer> ratings1, Map<Integer, Integer> ratings2) {
        int similarityScore = 0;
        for (Map.Entry<Integer, Integer> entry : ratings1.entrySet()) {
            Integer filmId = entry.getKey();
            Integer mark1 = entry.getValue();
            if (ratings2.containsKey(filmId)) {
                Integer mark2 = ratings2.get(filmId);
                similarityScore += Math.abs(mark1 - mark2);
            }
        }
        return similarityScore;
    }

    private Set<Integer> getMoviesOfSimilarUsers(Map<Integer, Map<Integer,Integer>> allMarkedFilms, List<Integer> similarUsers) {
        Set<Integer> moviesOfSimilarUsers = new HashSet<>();
        for (Integer user : similarUsers) {
            Map<Integer, Integer> userMarks = allMarkedFilms.get(user);
            if (userMarks != null) {
                moviesOfSimilarUsers.addAll(userMarks.keySet());
            }
        }
        return moviesOfSimilarUsers;
    }

    public List<Event> getFeed(Integer userId) {
        getUserById(userId);
        return eventService.getFeed(userId);
    }
}