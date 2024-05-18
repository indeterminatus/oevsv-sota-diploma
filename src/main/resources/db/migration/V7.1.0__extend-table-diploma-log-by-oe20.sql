ALTER TABLE diplomalog
    ADD COLUMN oe20 INTEGER DEFAULT NULL;

CREATE UNIQUE INDEX idx_diplomalog_oe20_unique ON diplomalog (oe20) WHERE oe20 IS NOT NULL;

CREATE OR REPLACE FUNCTION assign_oe20_seq()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.category = 'OE20SOTA' THEN
        NEW.oe20 := NEXTVAL('oe20_sequence');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_oe20
    BEFORE INSERT ON diplomalog
    FOR EACH ROW
EXECUTE FUNCTION assign_oe20_seq();
