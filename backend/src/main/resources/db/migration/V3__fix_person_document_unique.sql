ALTER TABLE person DROP CONSTRAINT person_document_number_key;
ALTER TABLE person ALTER COLUMN document_number TYPE VARCHAR(30);
ALTER TABLE person ADD CONSTRAINT person_document_unique UNIQUE (document_type, document_number);
