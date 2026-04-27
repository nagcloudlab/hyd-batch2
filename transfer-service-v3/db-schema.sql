
CREATE TABLE accounts (
    number VARCHAR(255) PRIMARY KEY,
    balance DECIMAL(19, 4) NOT NULL
);

INSERT INTO accounts (number, balance) VALUES ('123', 1000.00);
INSERT INTO accounts (number, balance) VALUES ('456', 500.00);

SELECT * FROM accounts;