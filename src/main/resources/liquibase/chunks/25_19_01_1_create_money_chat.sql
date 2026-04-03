create table if not exists money_chat
(
    id SERIAL NOT NULL,
    name TEXT UNIQUE NOT NULL,
    chat_id BIGINT UNIQUE NOT NULL,

    CONSTRAINT pk_money_chat_id PRIMARY KEY (id)
);