--liquibase formatted sql

--changeset tri:seed-person-data
INSERT INTO persons (first_name, last_name, date_of_birth, tax_id, tax_debt)
VALUES ('Michael', 'Johnson', '1980-05-15', 'TAX000001', 0.00),
       ('Michelle', 'Smith', '1990-08-20', 'TAX000002', 0.00),
       ('Robert', 'Mitchell', '1985-02-10', 'TAX000003', 0.00),
       ('Jennifer', 'Davis', '1975-11-05', 'TAX000004', 0.00);
