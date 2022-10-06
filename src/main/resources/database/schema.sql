DROP TABLE IF EXISTS USERS, REQUESTS, ITEMS, BOOKINGS, COMMENTS;

CREATE TABLE IF NOT EXISTS USERS
(
    ID BIGSERIAL PRIMARY KEY,
    NAME VARCHAR,
    EMAIL VARCHAR UNIQUE
);

CREATE TABLE IF NOT EXISTS REQUESTS
(
    ID BIGSERIAL PRIMARY KEY,
    DESCRIPTION VARCHAR,
    REQUEST_ID INTEGER
);

CREATE TABLE IF NOT EXISTS ITEMS
(
    ID BIGSERIAL PRIMARY KEY,
    NAME VARCHAR,
    DESCRIPTION VARCHAR,
    IS_AVAILABLE BOOLEAN,
    OWNER_ID INTEGER,
    REQUEST_ID INTEGER,
    FOREIGN KEY (OWNER_ID) REFERENCES USERS (ID),
    FOREIGN KEY (REQUEST_ID) REFERENCES REQUESTS (ID)
);

CREATE TABLE IF NOT EXISTS BOOKINGS
(
    ID BIGSERIAL PRIMARY KEY,
    START_DATE TIMESTAMP,
    END_DATE TIMESTAMP,
    ITEM_ID INTEGER,
    BOOKER_ID INTEGER,
    STATUS VARCHAR
);



CREATE TABLE IF NOT EXISTS COMMENTS
(
    ID BIGSERIAL PRIMARY KEY,
    TEXT VARCHAR,
    ITEM_ID INTEGER,
    AUTHOR_ID INTEGER,
    CREATED TIMESTAMP
);