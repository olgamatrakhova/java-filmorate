package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ReviewValidationException extends RuntimeException {
    public ReviewValidationException(String message) {
        super(message);
    }
}