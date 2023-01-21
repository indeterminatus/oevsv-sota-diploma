CREATE TABLE UserTable
(
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    role     VARCHAR(255),
    CONSTRAINT pk_user PRIMARY KEY (username)
);
