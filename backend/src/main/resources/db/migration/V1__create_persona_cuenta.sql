CREATE TABLE persona (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         nombre VARCHAR(200) NOT NULL,
                         documento VARCHAR(20) NOT NULL UNIQUE,
                         tipo_documento VARCHAR(20) NOT NULL,
                         email VARCHAR(150) NOT NULL UNIQUE,
                         telefono VARCHAR(20),
                         tipo VARCHAR(20) NOT NULL, -- CLIENTE | EMPLEADO
                         created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE cuenta (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        persona_id UUID NOT NULL REFERENCES persona(id),
                        numero VARCHAR(20) NOT NULL UNIQUE,
                        tipo VARCHAR(20) NOT NULL, -- CORRIENTE | AHORROS | NOMINA | TESORERIA | PROVEEDOR
                        saldo DECIMAL(15,2) NOT NULL DEFAULT 0,
                        moneda VARCHAR(3) NOT NULL DEFAULT 'COP',
                        estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
                        version BIGINT NOT NULL DEFAULT 0, -- Optimistic locking
                        created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_cuenta_persona ON cuenta(persona_id);
CREATE INDEX idx_cuenta_numero ON cuenta(numero);