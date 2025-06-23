--liquibase formatted sql

--changeset tri:init-person-table
CREATE TABLE persons
(
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(100)   NOT NULL,
    last_name     VARCHAR(100)   NOT NULL,
    date_of_birth DATE           NOT NULL,
    tax_id        VARCHAR(50)    NOT NULL UNIQUE,
    tax_debt      NUMERIC(15, 2) NOT NULL
);

--changeset tri:init-tax_seq-table
CREATE SEQUENCE tax_id_seq START WITH 81 INCREMENT BY 1;
