
CREATE TABLE IF NOT EXISTS INDEX_FILE(
    id BIGINT,
    session_id varchar(40),
    name varchar(500),
    cached boolean,
    primary key (id, session_id, name)
)