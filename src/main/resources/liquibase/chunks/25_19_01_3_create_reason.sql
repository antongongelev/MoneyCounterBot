create table if not exists reason
(
    id SERIAL NOT NULL,
    name TEXT UNIQUE NOT NULL,

    CONSTRAINT pk_reason_id PRIMARY KEY (id)
);