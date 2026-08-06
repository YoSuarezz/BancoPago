ALTER TABLE person
    ADD COLUMN client_number     VARCHAR(50),
    ADD COLUMN membership_date   DATE,
    ADD COLUMN position          VARCHAR(100),
    ADD COLUMN area              VARCHAR(100),
    ADD COLUMN cost_center       VARCHAR(50),
    ADD COLUMN contract_type     VARCHAR(30);
