CREATE TABLE IF NOT EXISTS users (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    user_name          TEXT NOT NULL UNIQUE,
    nick_name          TEXT,
    secret_public_key  TEXT NOT NULL,
    secret_private_key TEXT NOT NULL,
    storage_home_dir   TEXT,
    permission_nas     BOOLEAN,
    permission_iot     BOOLEAN
);

CREATE INDEX id                 ON users (id);
CREATE INDEX user_name          ON users (user_name);
CREATE INDEX secret_public_key  ON users (secret_public_key);
CREATE INDEX secret_private_key ON users (user_name);