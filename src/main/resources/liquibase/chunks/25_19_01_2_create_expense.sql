create table if not exists expense
(
    id SERIAL NOT NULL,
    money_chat_id BIGINT NOT NULL,
    expense_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    category TEXT NOT NULL,

    CONSTRAINT pk_expense_id PRIMARY KEY (id),
    CONSTRAINT fk_expense_money_chat_id FOREIGN KEY (money_chat_id) REFERENCES money_chat (chat_id) ON DELETE CASCADE
);