package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaService mpaService;

    @GetMapping()
    public Collection<Mpa> getMpa() {
        log.info("Запрос на получение списка рейтингов (getMpa())");
        return mpaService.getMpa();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) throws NotFoundException {
        log.info("Запрос на получение списка рейтингов по id (getMpa({}))", id);
        return mpaService.getMpaById(id);
    }
}