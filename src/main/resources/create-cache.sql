
CREATE TABLE IF NOT EXISTS CACHE_ENTRIES(
    id BIGINT,
    session_id varchar(40),
    entry bytea,
    primary key (id, session_id)
)