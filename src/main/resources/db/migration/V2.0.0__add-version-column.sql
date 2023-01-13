ALTER TABLE SummitList
    ADD version INTEGER NOT NULL default 1;

ALTER TABLE SummitListUpdateLog
    ADD version INTEGER NOT NULL default 1;
