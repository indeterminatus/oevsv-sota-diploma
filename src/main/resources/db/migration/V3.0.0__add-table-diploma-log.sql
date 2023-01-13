CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE DiplomaLog
(
    id           BIGINT  NOT NULL,
    callSign     VARCHAR(15),
    mail         VARCHAR(100),
    name         VARCHAR(150),
    category     VARCHAR(15),
    rank         VARCHAR(10),
    creationDate date,
    OE1          INTEGER NOT NULL,
    OE2          INTEGER NOT NULL,
    OE3          INTEGER NOT NULL,
    OE4          INTEGER NOT NULL,
    OE5          INTEGER NOT NULL,
    OE6          INTEGER NOT NULL,
    OE7          INTEGER NOT NULL,
    OE8          INTEGER NOT NULL,
    OE9          INTEGER NOT NULL,
    CONSTRAINT pk_diplomalog PRIMARY KEY (id)
);
