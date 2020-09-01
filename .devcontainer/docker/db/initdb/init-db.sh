#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
CREATE USER docker;
CREATE DATABASE docker;
GRANT ALL PRIVILEGES ON DATABASE docker TO docker;

EOSQL

psql -v ON_ERROR_STOP=1 --username do"$POSTGRES_USER" cker --dbname docker <<-EOSQL
CREATE TABLE users (
  id                SERIAL PRIMARY KEY,
  user_name         VARCHAR(20) NOT NULL,
  email             VARCHAR(100),
  password          CHAR(64) NOT NULL
);

CREATE TABLE user_status (
  id                SERIAL PRIMARY KEY,
  user_id           INTEGER NOT NULL,
  status            VARCHAR(20) NOT NULL,
  last_updated_at   TIMESTAMP WITH TIME ZONE
);

CREATE TABLE user_items (
  id                SERIAL PRIMARY KEY,
  user_id           INTEGER NOT NULL,
  purchase_date     DATE NOT NULL,
  item_code         INTEGER NOT NULL,
  item_name         VARCHAR(100),
  last_updated_at   TIMESTAMP WITH TIME ZONE
);

EOSQL
