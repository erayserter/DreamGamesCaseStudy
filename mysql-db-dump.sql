CREATE TABLE if not exists country (
    id INT PRIMARY KEY,
    code VARCHAR(5),
    name VARCHAR(100)
);

INSERT INTO `country` (`id`, `code`, `name`)
VALUES (1, 'TR', 'Turkey'),
       (2, 'US', 'The United States'),
       (3, 'GB', 'The United Kingdom'),
       (4, 'FR', 'France'),
       (5, 'DE', 'Germany');
