create table if not exists expense
(
    id SERIAL NOT NULL,
    money_chat_name TEXT NOT NULL,
    telegram_id BIGINT NOT NULL,
    username TEXT NOT NULL,
    expense_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    reason TEXT NOT NULL,

    CONSTRAINT pk_expense_id PRIMARY KEY (id),
    CONSTRAINT fk_expense_money_chat_name FOREIGN KEY (money_chat_name) REFERENCES money_chat (name) ON DELETE CASCADE
);