create table if not exists category
(
    id SERIAL NOT NULL,
    name TEXT UNIQUE NOT NULL,
    money_chat_id BIGINT NOT NULL,

    CONSTRAINT pk_category_id PRIMARY KEY (id),
    CONSTRAINT fk_category_money_chat_id FOREIGN KEY (money_chat_id) REFERENCES money_chat (chat_id) ON DELETE CASCADE
);