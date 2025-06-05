--liquibase formatted sql

--changeset tri:sql-1
CREATE TABLE persons (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    tax_debt NUMERIC(15, 2) NOT NULL
);
