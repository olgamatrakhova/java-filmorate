DROP TABLE IF EXISTS likes CASCADE;

DROP TABLE IF EXISTS friends CASCADE;

DROP TABLE IF EXISTS users CASCADE;

DROP TABLE IF EXISTS film_genres CASCADE;

DROP TABLE IF EXISTS genres CASCADE;

DROP TABLE IF EXISTS film_directors CASCADE;

DROP TABLE IF EXISTS directors CASCADE;

DROP TABLE IF EXISTS films CASCADE;

DROP TABLE IF EXISTS mpa CASCADE;

DROP TABLE IF EXISTS reviews CASCADE;

DROP TABLE IF EXISTS likes_review CASCADE;


CREATE TABLE IF NOT EXISTS mpa
(
    mpa_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS films
(
    film_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL,
    description VARCHAR(300),
    release_dt DATE,
    duration INTEGER,
    rate INTEGER,
    mpa_id INTEGER REFERENCES mpa (mpa_id)
);

CREATE TABLE IF NOT EXISTS genres
(
    genre_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS film_genres
(
    film_id INTEGER NOT NULL REFERENCES films (film_id),
    genre_id INTEGER NOT NULL REFERENCES genres (genre_id)
);

CREATE TABLE IF NOT EXISTS users
(
    user_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR NOT NULL UNIQUE,
    login VARCHAR NOT NULL UNIQUE,
    name VARCHAR NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS friends
(
    from_user_id INTEGER NOT NULL REFERENCES users (user_id),
    to_user_id INTEGER NOT NULL REFERENCES users (user_id),
    status BOOLEAN NOT NULL,
    PRIMARY KEY (from_user_id, to_user_id)
);

CREATE TABLE IF NOT EXISTS likes
(
    film_id INTEGER NOT NULL REFERENCES films (film_id),
    user_id INTEGER NOT NULL REFERENCES users (user_id)
);

CREATE TABLE  IF NOT EXISTS directors (
  director_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS film_directors
(
    film_id INTEGER NOT NULL REFERENCES films (film_id),
    director_id INTEGER NOT NULL REFERENCES directors (director_id)
);


CREATE TABLE IF NOT EXISTS reviews
(
review_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
content varchar(400) NOT NULL,
is_positive boolean NOT NULL,
user_id INTEGER NOT NULL REFERENCES users(user_id),
film_id INTEGER NOT NULL REFERENCES films(film_id),
useful INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS likes_review
(
review_id INTEGER NOT NULL REFERENCES reviews,
user_id INTEGER NOT NULL REFERENCES users,
is_useful boolean NOT NULL,
CONSTRAINT unique_ids UNIQUE(review_id, user_id)
);