#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
CREATE USER docker;
CREATE DATABASE docker;
GRANT ALL PRIVILEGES ON DATABASE docker TO docker;

EOSQL

psql -v ON_ERROR_STOP=1 --username docker --dbname docker <<-EOSQL
CREATE TABLE users (
  account_id        SERIAL PRIMARY KEY,
  account_name      VARCHAR(20),
  email             VARCHAR(100),
  password    CHAR(64)
);

CREATE TABLE userstatus (
  status            VARCHAR(20) PRIMARY KEY
);

EOSQL
