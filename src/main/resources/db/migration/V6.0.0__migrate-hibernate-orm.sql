CREATE SEQUENCE IF NOT EXISTS summitlistupdateLog_seq START WITH 1 INCREMENT BY 1;
SELECT setval('summitlistupdateLog_seq',  (SELECT MAX(id) FROM summitlistupdatelog));
