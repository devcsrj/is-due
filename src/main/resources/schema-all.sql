CREATE TABLE IF NOT EXISTS invoices
(
    id             VARCHAR(32)    NOT NULL,
    biller_id      VARCHAR(32)    NOT NULL,
    biller_name    VARCHAR(255)   NOT NULL,
    account_number VARCHAR(32)    NOT NULL,
    issued_at      TIMESTAMP      NOT NULL,
    due_at         TIMESTAMP      NOT NULL,
    total          DECIMAL(10, 2) NOT NULL
);
