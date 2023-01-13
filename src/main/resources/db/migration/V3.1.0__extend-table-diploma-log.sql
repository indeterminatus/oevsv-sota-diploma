ALTER TABLE DiplomaLog
    ADD version INTEGER NOT NULL default 1,
    ADD reviewMailSent BOOLEAN NOT NULL default false;
