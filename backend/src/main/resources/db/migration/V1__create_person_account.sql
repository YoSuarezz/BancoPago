CREATE TABLE person (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    document_number VARCHAR(20) NOT NULL UNIQUE,
    document_type VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    person_type VARCHAR(20) NOT NULL, -- CLIENT | EMPLOYEE
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id UUID NOT NULL REFERENCES person(id),
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL, -- CHECKING | SAVINGS | PAYROLL | TREASURY | SUPPLIER
    balance DECIMAL(15,2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'COP',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic locking
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_account_person ON account(person_id);
CREATE INDEX idx_account_number ON account(account_number);
